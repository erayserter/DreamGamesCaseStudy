package com.dreamgames.backendengineeringcasestudy.repository;

import com.dreamgames.backendengineeringcasestudy.model.Country;
import com.dreamgames.backendengineeringcasestudy.model.Tournament;
import com.dreamgames.backendengineeringcasestudy.model.TournamentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TournamentGroupRepository extends JpaRepository<TournamentGroup, Long> {
    @Query("SELECT tg " +
            "FROM TournamentGroup as tg " +
            "WHERE tg.tournament = :tournament " +
            "AND NOT EXISTS (SELECT utg " +
                            "FROM UserTournamentGroup as utg " +
                            "WHERE utg.tournamentGroup = tg " +
                            "AND utg.user.country = :country)")
    Optional<TournamentGroup> findHasNotUserWithCountryInTournament(Tournament tournament, Country country);
}
