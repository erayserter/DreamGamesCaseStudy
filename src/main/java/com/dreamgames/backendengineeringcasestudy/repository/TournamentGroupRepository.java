package com.dreamgames.backendengineeringcasestudy.repository;

import com.dreamgames.backendengineeringcasestudy.model.Country;
import com.dreamgames.backendengineeringcasestudy.model.Tournament;
import com.dreamgames.backendengineeringcasestudy.model.TournamentGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TournamentGroupRepository extends JpaRepository<TournamentGroup, Long> {
    Optional<TournamentGroup> findByTournamentAndUserTournamentGroups_User_CountryNot(Tournament tournament, Country country);
}
