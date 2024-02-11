package com.dreamgames.backendengineeringcasestudy.repository;

import com.dreamgames.backendengineeringcasestudy.model.Tournament;
import com.dreamgames.backendengineeringcasestudy.model.TournamentGroup;
import com.dreamgames.backendengineeringcasestudy.model.User;
import com.dreamgames.backendengineeringcasestudy.model.UserTournamentGroup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
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

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void itShouldFindByUserIdAndTournamentId() {
        // given
        User user = new User();
        user = userRepository.save(user);
        Tournament tournament = new Tournament();
        tournament = tournamentRepository.save(tournament);
        TournamentGroup tournamentGroup = new TournamentGroup(tournament);
        tournamentGroup = tournamentGroupRepository.save(tournamentGroup);
        UserTournamentGroup userTournamentGroup = new UserTournamentGroup(user, tournamentGroup, 1);
        underTest.save(userTournamentGroup);

        // when
        Optional<UserTournamentGroup> expected =
                underTest.findByUserIdAndTournamentId(user.getId(), tournament.getId());

        // then
        assertThat(expected).isPresent();
    }

    @Test
    void itShouldNotFindByUserIdAndTournamentId() {
        // given
        User user = new User();
        user = userRepository.save(user);
        Tournament tournament = new Tournament();
        tournament = tournamentRepository.save(tournament);
        TournamentGroup tournamentGroup = new TournamentGroup(tournament);
        tournamentGroup = tournamentGroupRepository.save(tournamentGroup);
        UserTournamentGroup userTournamentGroup = new UserTournamentGroup(user, tournamentGroup, 1);
        underTest.save(userTournamentGroup);

        // when
        Optional<UserTournamentGroup> expected =
                underTest.findByUserIdAndTournamentId(user.getId(), 2L);

        // then
        assertThat(expected).isNotPresent();
    }

    @Test
    void itShouldFindByTournamentGroupAndRanking() {
        // given
        User user = new User();
        user = userRepository.save(user);
        Tournament tournament = new Tournament();
        tournament = tournamentRepository.save(tournament);
        TournamentGroup tournamentGroup = new TournamentGroup(tournament);
        tournamentGroup = tournamentGroupRepository.save(tournamentGroup);
        UserTournamentGroup userTournamentGroup = new UserTournamentGroup(user, tournamentGroup, 3);
        underTest.save(userTournamentGroup);

        // when
        Optional<UserTournamentGroup> expected =
                underTest.findByTournamentGroupAndRanking(tournamentGroup, 3);

        // then
        assertThat(expected).isPresent();
        assertThat(expected.get()).isEqualTo(userTournamentGroup);
    }

    @Test
    void itShouldNotFindByTournamentGroupAndRanking() {
        // given
        User user = new User();
        user = userRepository.save(user);
        Tournament tournament = new Tournament();
        tournament = tournamentRepository.save(tournament);
        TournamentGroup tournamentGroup = new TournamentGroup(tournament);
        tournamentGroup = tournamentGroupRepository.save(tournamentGroup);
        UserTournamentGroup userTournamentGroup = new UserTournamentGroup(user, tournamentGroup, 3);
        underTest.save(userTournamentGroup);

        // when
        Optional<UserTournamentGroup> expected =
                underTest.findByTournamentGroupAndRanking(tournamentGroup, 2);

        // then
        assertThat(expected).isNotPresent();
    }

    @Test
    void itShouldFindPreviousUnclaimedTournamentRewards() {
        // given
        User user = new User();
        user = userRepository.save(user);
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime start = now.minusDays(2);
        ZonedDateTime end = now.minusDays(1);
        Tournament tournament = new Tournament(Date.from(start.toInstant()), Date.from(end.toInstant()));
        tournament = tournamentRepository.save(tournament);
        TournamentGroup tournamentGroup = new TournamentGroup(tournament);
        tournamentGroup = tournamentGroupRepository.save(tournamentGroup);
        UserTournamentGroup userTournamentGroup = new UserTournamentGroup(user, tournamentGroup, 3);
        underTest.save(userTournamentGroup);

        // when
        List<UserTournamentGroup> expected =
                underTest.findPreviousUnclaimedTournamentRewards(
                        user.getId(),
                        3,
                        false,
                        Date.from(now.toInstant())
                );

        // then
        assertThat(expected).isNotEmpty();
    }

    @Test
    void itShouldNotFindPreviousUnclaimedTournamentRewards() {
        // given
        User user = new User();
        user = userRepository.save(user);

        // when
        List<UserTournamentGroup> expected =
                underTest.findPreviousUnclaimedTournamentRewards(
                        user.getId(),
                        3,
                        false,
                        Date.from(Instant.now())
                );

        // then
        assertThat(expected).isEmpty();
    }
}