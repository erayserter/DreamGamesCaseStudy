package com.dreamgames.backendengineeringcasestudy.repository;

import com.dreamgames.backendengineeringcasestudy.dto.CountryTournamentScoreResponse;
import com.dreamgames.backendengineeringcasestudy.model.TournamentGroup;
import com.dreamgames.backendengineeringcasestudy.model.UserTournamentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserTournamentGroupRepository extends JpaRepository<UserTournamentGroup, Long> {
    @Query("SELECT utg " +
            "FROM UserTournamentGroup utg " +
            "WHERE utg.user.id = :userId AND utg.tournamentGroup.tournament.id = :tournamentId")
    Optional<UserTournamentGroup> findByUserIdAndTournamentId(@Param("userId") UUID userId,
                                                              @Param("tournamentId") Long tournamentId);

    @Query("SELECT utg " +
            "FROM UserTournamentGroup utg " +
            "WHERE utg.user.id = :userId " +
            "AND utg.isRewardClaimed = :isRewardClaimed " +
            "AND utg.tournamentGroup.tournament.endDateTime < :date")
    List<UserTournamentGroup> findPreviousUnclaimedTournamentRewards(@Param("userId") UUID userId,
                                                                     @Param("isRewardClaimed") boolean isRewardClaimed,
                                                                     @Param("date") Date date);

    @Query("SELECT utg " +
            "FROM UserTournamentGroup utg " +
            "WHERE utg.tournamentGroup.id = :id " +
            "ORDER BY utg.score DESC")
    List<UserTournamentGroup> orderGroupByScores(Long id);

    @Query("SELECT new com.dreamgames.backendengineeringcasestudy.dto.CountryTournamentScoreResponse(utg.user.country.name, SUM(utg.score)) " +
            "FROM UserTournamentGroup utg " +
            "WHERE utg.tournamentGroup.tournament.id = :tournamentId " +
            "GROUP BY utg.user.country.name " +
            "ORDER BY SUM(utg.score) DESC")
    List<CountryTournamentScoreResponse> findCountryScoresByTournamentId(Long tournamentId);
}