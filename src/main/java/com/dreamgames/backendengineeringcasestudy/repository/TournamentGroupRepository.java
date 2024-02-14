package com.dreamgames.backendengineeringcasestudy.repository;

import com.dreamgames.backendengineeringcasestudy.model.Country;
import com.dreamgames.backendengineeringcasestudy.model.Tournament;
import com.dreamgames.backendengineeringcasestudy.model.TournamentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TournamentGroupRepository extends JpaRepository<TournamentGroup, Long> {
    @Query("SELECT tg " +
            "FROM TournamentGroup tg " +
            "WHERE tg.tournament = :tournament " +
            "AND tg NOT IN (" +
                            "SELECT utg.tournamentGroup " +
                            "FROM UserTournamentGroup utg " +
                            "WHERE utg.user.country = :country" +
                            ")")
    Optional<TournamentGroup> findHasNoUsersWithCountry(@Param("tournament") Tournament tournament,
                                                        @Param("country") Country country);
}
