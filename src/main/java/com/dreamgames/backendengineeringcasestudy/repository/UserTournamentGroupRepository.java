package com.dreamgames.backendengineeringcasestudy.repository;

import com.dreamgames.backendengineeringcasestudy.model.TournamentGroup;
import com.dreamgames.backendengineeringcasestudy.model.User;
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
    UserTournamentGroup findByTournamentGroupAndRanking(TournamentGroup tournamentGroup, int rank);

    @Query("SELECT utg " +
            "FROM UserTournamentGroup utg " +
            "WHERE utg.user.id = :userId " +
            "AND utg.ranking = :rank " +
            "AND utg.isRewardClaimed = :isRewardClaimed " +
            "AND utg.tournamentGroup.tournament.endDateTime < :date")
    List<UserTournamentGroup> findPreviousUnclaimedTournamentRewards(@Param("userId") UUID userId,
                                                                     @Param("rank") int rank,
                                                                     @Param("isRewardClaimed") boolean isRewardClaimed,
                                                                     @Param("date") Date date);
}
