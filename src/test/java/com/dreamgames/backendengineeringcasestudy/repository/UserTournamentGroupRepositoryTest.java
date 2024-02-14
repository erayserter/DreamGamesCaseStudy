package com.dreamgames.backendengineeringcasestudy.repository;

import com.dreamgames.backendengineeringcasestudy.dto.CountryTournamentScoreResponse;
import com.dreamgames.backendengineeringcasestudy.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@DataJpaTest
class UserTournamentGroupRepositoryTest {

    @Autowired
    private UserTournamentGroupRepository underTest;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private TournamentGroupRepository tournamentGroupRepository;
    @Autowired
    private CountryRepository countryRepository;

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
        userRepository.deleteAll();
        tournamentRepository.deleteAll();
        tournamentGroupRepository.deleteAll();
        countryRepository.deleteAll();
    }

    @Test
    void shouldFindByUserIdAndTournamentId() {
        // given
        User user = new User();
        user = userRepository.save(user);
        Tournament tournament = new Tournament();
        tournament = tournamentRepository.save(tournament);
        TournamentGroup tournamentGroup = new TournamentGroup(tournament);
        tournamentGroup = tournamentGroupRepository.save(tournamentGroup);
        UserTournamentGroup userTournamentGroup = new UserTournamentGroup(user, tournamentGroup);
        underTest.save(userTournamentGroup);

        // when
        Optional<UserTournamentGroup> expected =
                underTest.findByUserIdAndTournamentId(user.getId(), tournament.getId());

        // then
        assertThat(expected).isPresent();
    }

    @Test
    void shouldNotFindByUserIdAndTournamentId() {
        // given
        User user = new User();
        user = userRepository.save(user);
        Tournament tournament = new Tournament();
        tournament = tournamentRepository.save(tournament);
        TournamentGroup tournamentGroup = new TournamentGroup(tournament);
        tournamentGroup = tournamentGroupRepository.save(tournamentGroup);
        UserTournamentGroup userTournamentGroup = new UserTournamentGroup(user, tournamentGroup);
        underTest.save(userTournamentGroup);

        // when
        Optional<UserTournamentGroup> expected =
                underTest.findByUserIdAndTournamentId(user.getId(), 2L);

        // then
        assertThat(expected).isNotPresent();
    }

    @Test
    void shouldFindPreviousUnclaimedTournamentRewards() {
        // given
        User user = new User();
        user = userRepository.save(user);
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime start = now.minusDays(2);
        ZonedDateTime end = now.minusDays(1);
        Tournament tournament = Tournament
                .builder()
                .startDateTime(Date.from(start.toInstant()))
                .endDateTime(Date.from(end.toInstant()))
                .build();
        tournament = tournamentRepository.save(tournament);
        TournamentGroup tournamentGroup = new TournamentGroup(tournament);
        tournamentGroup = tournamentGroupRepository.save(tournamentGroup);
        UserTournamentGroup userTournamentGroup = new UserTournamentGroup(user, tournamentGroup);
        underTest.save(userTournamentGroup);

        // when
        List<UserTournamentGroup> expected =
                underTest.findPreviousUnclaimedTournamentRewards(
                        user.getId(),
                        false,
                        Date.from(now.toInstant())
                );

        // then
        assertThat(expected).isNotEmpty();
    }

    @Test
    void shouldNotFindPreviousUnclaimedTournamentRewards() {
        // given
        User user = new User();
        user = userRepository.save(user);

        // when
        List<UserTournamentGroup> expected =
                underTest.findPreviousUnclaimedTournamentRewards(
                        user.getId(),
                        false,
                        Date.from(Instant.now())
                );

        // then
        assertThat(expected).isEmpty();
    }

    @Test
    void shouldOrderGroupByScores() {
        // given
        User user = new User();
        User anotherUser = new User();
        user = userRepository.save(user);
        anotherUser = userRepository.save(anotherUser);
        Tournament tournament = new Tournament();
        tournament = tournamentRepository.save(tournament);
        TournamentGroup tournamentGroup = new TournamentGroup(tournament);
        tournamentGroup = tournamentGroupRepository.save(tournamentGroup);
        UserTournamentGroup userTournamentGroup1 = new UserTournamentGroup(user, tournamentGroup);
        userTournamentGroup1.setScore(10);
        UserTournamentGroup userTournamentGroup2 = new UserTournamentGroup(anotherUser, tournamentGroup);
        userTournamentGroup2.setScore(20);
        underTest.save(userTournamentGroup1);
        underTest.save(userTournamentGroup2);

        // when
        List<UserTournamentGroup> expected = underTest.orderGroupByScores(tournamentGroup.getId());

        // then
        assertThat(expected).isNotEmpty();
        assertThat(expected.get(0)).isEqualTo(userTournamentGroup2);
        assertThat(expected.get(1)).isEqualTo(userTournamentGroup1);
    }

    @Test
    void shouldFindCountryScoresByTournamentId() {
        // given
        Country country = new Country("TR", "Turkey");
        country = countryRepository.save(country);
        User user = new User(country);
        user = userRepository.save(user);
        Tournament tournament = new Tournament();
        tournament = tournamentRepository.save(tournament);
        TournamentGroup tournamentGroup = new TournamentGroup(tournament);
        tournamentGroup = tournamentGroupRepository.save(tournamentGroup);
        UserTournamentGroup userTournamentGroup = new UserTournamentGroup(user, tournamentGroup);
        userTournamentGroup.setScore(10);
        underTest.save(userTournamentGroup);

        // when
        List<CountryTournamentScoreResponse> expected = underTest.findCountryScoresByTournamentId(tournament.getId());

        // then
        assertThat(expected).isNotEmpty();
        assertThat(expected.get(0).country()).isEqualTo(user.getCountry().getName());
        assertThat(expected.get(0).score()).isEqualTo(10);
    }
}