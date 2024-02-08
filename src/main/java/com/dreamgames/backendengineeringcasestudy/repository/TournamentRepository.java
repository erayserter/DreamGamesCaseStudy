package com.dreamgames.backendengineeringcasestudy.repository;


import com.dreamgames.backendengineeringcasestudy.model.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;


@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    @Query("SELECT t FROM Tournament t WHERE t.startDateTime <= :now AND t.endDateTime > :now")
    Optional<Tournament> findOngoingTournament(@Param("now") Date now);
}
