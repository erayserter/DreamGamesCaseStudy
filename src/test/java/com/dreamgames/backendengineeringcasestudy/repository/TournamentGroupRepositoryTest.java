package com.dreamgames.backendengineeringcasestudy.repository;

import com.dreamgames.backendengineeringcasestudy.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TournamentGroupRepositoryTest {
    @Autowired
    private CountryRepository countryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private TournamentGroupRepository tournamentGroupRepository;
    @Autowired
    private UserTournamentGroupRepository userTournamentGroupRepository;

    @Autowired
    private TournamentGroupRepository underTest;

    private Country country;
    private Tournament tournament;
    private TournamentGroup tournamentGroup;

    @BeforeEach
    void setUp() {
        country = new Country("TR", "Turkey");
        User user = User.builder()
                .id(UUID.randomUUID())
                .country(country)
                .build();
        tournament = Tournament.builder().id(1L).build();
        tournamentGroup = TournamentGroup.builder().tournament(tournament).build();
        UserTournamentGroup userTournamentGroup = UserTournamentGroup.builder()
                .user(user)
                .tournamentGroup(tournamentGroup)
                .build();

        countryRepository.save(country);
        userRepository.save(user);
        tournamentRepository.save(tournament);
        tournamentGroupRepository.save(tournamentGroup);
        userTournamentGroupRepository.save(userTournamentGroup);
    }

    @AfterEach
    void tearDown() {
        userTournamentGroupRepository.deleteAll();
        tournamentGroupRepository.deleteAll();
        tournamentRepository.deleteAll();
        userRepository.deleteAll();
        countryRepository.deleteAll();
        underTest.deleteAll();
    }

    @Test
    void findHasNotUserWithCountryInTournamentWithDifferentCountries() {
        // given
        Country country = new Country("US", "United States");
        User anotherUser = User.builder()
                .id(UUID.randomUUID())
                .country(country)
                .build();

        countryRepository.save(country);
        userRepository.save(anotherUser);

        // when
        Optional<TournamentGroup> expected = underTest.findHasNotUserWithCountryInTournament(tournament, anotherUser.getCountry());

        // then
        assertThat(expected).isPresent();
        TournamentGroup expectedTournamentGroup = expected.get();
        assertThat(expectedTournamentGroup.getId()).isEqualTo(tournamentGroup.getId());
    }

    @Test
    void findHasNotUserWithCountryInTournamentWithSameCountry() {
        // given
        User anotherUser = User.builder()
                .id(UUID.randomUUID())
                .country(country)
                .build();

        countryRepository.save(country);
        userRepository.save(anotherUser);

        // when
        Optional<TournamentGroup> expected = underTest.findHasNotUserWithCountryInTournament(tournament, anotherUser.getCountry());

        // then
        assertThat(expected).isEmpty();
    }
}