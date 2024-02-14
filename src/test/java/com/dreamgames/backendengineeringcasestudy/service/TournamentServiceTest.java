package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.model.*;
import com.dreamgames.backendengineeringcasestudy.repository.TournamentGroupRepository;
import com.dreamgames.backendengineeringcasestudy.repository.TournamentRepository;
import com.dreamgames.backendengineeringcasestudy.repository.UserRepository;
import com.dreamgames.backendengineeringcasestudy.repository.UserTournamentGroupRepository;
import org.hibernate.ObjectNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class TournamentServiceTest {

    @Mock private TournamentRepository tournamentRepository;
    @Mock private TournamentGroupRepository tournamentGroupRepository;
    @Mock private UserTournamentGroupRepository userTournamentGroupRepository;
    @Mock private UserRepository userRepository;
    private TournamentService underTest;

    @BeforeEach
    void setUp() {
        underTest = new TournamentService(
                tournamentRepository,
                tournamentGroupRepository,
                userTournamentGroupRepository,
                userRepository
        );
    }

    @Test
    void shouldEnterTournamentEmptyGroup() {
        // given
        User user = new User(new Country("TR", "Turkey"));
        user.setId(UUID.randomUUID());
        user.setLevel(TournamentService.TOURNAMENT_LEVEL_REQUIREMENT);
        user.setCoins(TournamentService.TOURNAMENT_ENTRY_FEE);
        Tournament tournament = new Tournament();
        TournamentGroup group = new TournamentGroup(tournament);

        TournamentService spy = Mockito.spy(underTest);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userTournamentGroupRepository
                .findPreviousUnclaimedTournamentRewards(
                        any(UUID.class),
                        any(Integer.class),
                        any(Boolean.class),
                        any(Date.class)
                ))
                .willReturn(List.of());
        given(spy.getCurrentTournament()).willReturn(tournament);
        given(userTournamentGroupRepository
                .findByUserIdAndTournamentId(user.getId(), tournament.getId()))
                .willReturn(Optional.empty());
        given(tournamentGroupRepository
                .findHasNoUsersWithCountry(tournament, user.getCountry()))
                .willReturn(Optional.empty());
        given(tournamentGroupRepository.save(any(TournamentGroup.class))).willReturn(group);

        // when
        spy.enterTournament(user.getId());

        // then
        ArgumentCaptor<UserTournamentGroup> userTournamentGroupArgumentCaptor = ArgumentCaptor.forClass(UserTournamentGroup.class);
        verify(userTournamentGroupRepository).save(userTournamentGroupArgumentCaptor.capture());
        UserTournamentGroup capturedUserTournamentGroup = userTournamentGroupArgumentCaptor.getValue();
        assertThat(capturedUserTournamentGroup.getUser()).isEqualTo(user);
        assertThat(capturedUserTournamentGroup.getTournamentGroup()).isEqualTo(group);
        assertThat(capturedUserTournamentGroup.getRanking()).isEqualTo(1);
    }

    @Test
    void shouldEnterTournamentLastAttendee() {
        // given
        User user = new User(new Country("TR", "Turkey"));
        user.setId(UUID.randomUUID());
        user.setLevel(TournamentService.TOURNAMENT_LEVEL_REQUIREMENT);
        user.setCoins(TournamentService.TOURNAMENT_ENTRY_FEE);
        Tournament tournament = new Tournament();
        TournamentGroup group = new TournamentGroup(tournament);
        List<UserTournamentGroup> users = new ArrayList<>();
        for (int i = 1; i < TournamentService.TOURNAMENT_GROUP_SIZE; i++) {
            users.add(new UserTournamentGroup(null, group, i));
        }
        group.setUserTournamentGroups(users);

        TournamentService spy = Mockito.spy(underTest);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userTournamentGroupRepository
                .findPreviousUnclaimedTournamentRewards(
                        any(UUID.class),
                        any(Integer.class),
                        any(Boolean.class),
                        any(Date.class)
                ))
                .willReturn(List.of());
        given(spy.getCurrentTournament()).willReturn(tournament);
        given(userTournamentGroupRepository
                .findByUserIdAndTournamentId(user.getId(), tournament.getId()))
                .willReturn(Optional.empty());
        given(tournamentGroupRepository
                .findHasNoUsersWithCountry(tournament, user.getCountry()))
                .willReturn(Optional.of(group));
        given(tournamentGroupRepository.save(any(TournamentGroup.class))).willReturn(group);

        // when
        spy.enterTournament(user.getId());

        // then
        ArgumentCaptor<UserTournamentGroup> userTournamentGroupArgumentCaptor = ArgumentCaptor.forClass(UserTournamentGroup.class);
        verify(userTournamentGroupRepository).save(userTournamentGroupArgumentCaptor.capture());
        UserTournamentGroup capturedUserTournamentGroup = userTournamentGroupArgumentCaptor.getValue();
        assertThat(capturedUserTournamentGroup.getUser()).isEqualTo(user);
        assertThat(capturedUserTournamentGroup.getTournamentGroup()).isEqualTo(group);
        assertThat(capturedUserTournamentGroup.getRanking()).isEqualTo(TournamentService.TOURNAMENT_GROUP_SIZE);
    }

    @Test
    void willThrowWhenEnterTournamentUserNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> underTest.enterTournament(userId))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    void willThrowWhenEnterTournamentUserLevelNotEnough() {
        // given
        User user = new User();
        user.setLevel(TournamentService.TOURNAMENT_LEVEL_REQUIREMENT - 1);
        given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(user));

        // when
        // then
        assertThatThrownBy(() -> underTest.enterTournament(UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User level is not enough to enter the tournament");
    }

    @Test
    void willThrowWhenEnterTournamentUserCoinsNotEnough() {
        // given
        User user = new User();
        user.setLevel(TournamentService.TOURNAMENT_LEVEL_REQUIREMENT);
        user.setCoins(TournamentService.TOURNAMENT_ENTRY_FEE - 1);
        given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(user));

        // when
        // then
        assertThatThrownBy(() -> underTest.enterTournament(UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User does not have enough coins to enter the tournament");
    }

    @Test
    void willThrowWhenEnterTournamentUserHasUnclaimedRewards() {
        // given
        User user = new User();
        user.setLevel(TournamentService.TOURNAMENT_LEVEL_REQUIREMENT);
        user.setCoins(TournamentService.TOURNAMENT_ENTRY_FEE);
        given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(user));
        given(userTournamentGroupRepository
                .findPreviousUnclaimedTournamentRewards(
                        any(UUID.class),
                        any(Integer.class),
                        any(Boolean.class),
                        any(Date.class)
                ))
                .willReturn(List.of(new UserTournamentGroup()));

        // when
        // then
        assertThatThrownBy(() -> underTest.enterTournament(UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User has unclaimed rewards");
    }

    @Test
    void willThrowWhenEnterTournamentUserAlreadyEntered() {
        // given
        User user = new User();
        user.setLevel(TournamentService.TOURNAMENT_LEVEL_REQUIREMENT);
        user.setCoins(TournamentService.TOURNAMENT_ENTRY_FEE);

        TournamentService spy = Mockito.spy(underTest);

        given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(user));
        given(userTournamentGroupRepository
                .findPreviousUnclaimedTournamentRewards(
                        any(UUID.class),
                        any(Integer.class),
                        any(Boolean.class),
                        any(Date.class)
                ))
                .willReturn(List.of());
        given(spy.getCurrentTournament()).willReturn(new Tournament());
        given(userTournamentGroupRepository
                .findByUserIdAndTournamentId(any(UUID.class), any()))
                .willReturn(Optional.of(new UserTournamentGroup()));

        // when
        // then
        assertThatThrownBy(() -> spy.enterTournament(UUID.randomUUID()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User has already entered the tournament");
    }

    @Test
    void shouldClaimReward() {
        // given
        int coins = 5000;
        Date date = Date.from(Instant.now().minus(Duration.ofDays(1)));
        User user = User.builder().id(UUID.randomUUID()).coins(coins).build();
        Tournament tournament = Tournament.builder().id(1L).endDateTime(date).build();
        TournamentGroup tournamentGroup = new TournamentGroup(tournament);
        UserTournamentGroup userTournamentGroup = new UserTournamentGroup(user, tournamentGroup, 1);

        given(userTournamentGroupRepository.findByUserIdAndTournamentId(user.getId(), tournament.getId()))
                .willReturn(Optional.of(userTournamentGroup));

        // when
        underTest.claimReward(tournament.getId(), user.getId());

        // then
        ArgumentCaptor<UserTournamentGroup> userTournamentGroupArgumentCaptor = ArgumentCaptor.forClass(UserTournamentGroup.class);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userTournamentGroupRepository).save(userTournamentGroupArgumentCaptor.capture());
        verify(userRepository).save(userArgumentCaptor.capture());
        UserTournamentGroup capturedUserTournamentGroup = userTournamentGroupArgumentCaptor.getValue();
        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUserTournamentGroup.isRewardClaimed()).isTrue();
        assertThat(capturedUser.getCoins()).isEqualTo(coins + 10000);
    }

    @Test
    void willThrowWhenClaimRewardUserTournamentNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        given(userTournamentGroupRepository.findByUserIdAndTournamentId(any(UUID.class), any(Long.class)))
                .willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> underTest.claimReward(1L, userId))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    void willThrowWhenClaimRewardTournamentNotEnded() {
        // given
        User user = new User();
        user.setId(UUID.randomUUID());
        Date date = Date.from(Instant.now().plus(Duration.ofDays(1)));
        Tournament tournament = Tournament.builder().endDateTime(date).build();
        TournamentGroup group = new TournamentGroup(tournament);
        given(userTournamentGroupRepository.findByUserIdAndTournamentId(user.getId(), tournament.getId()))
                .willReturn(Optional.of(new UserTournamentGroup(user, group, 1)));

        // when
        // then
        assertThatThrownBy(() -> underTest.claimReward(tournament.getId(), user.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tournament has not ended yet");
    }

    @Test
    void willThrowWhenClaimRewardRewardAlreadyClaimed() {
        // given
        User user = new User();
        user.setId(UUID.randomUUID());
        Date date = Date.from(Instant.now().minus(Duration.ofDays(1)));
        Tournament tournament = Tournament.builder().endDateTime(date).build();
        TournamentGroup group = new TournamentGroup(tournament);
        UserTournamentGroup userTournamentGroup = new UserTournamentGroup(user, group, 1);
        userTournamentGroup.setRewardClaimed(true);
        given(userTournamentGroupRepository.findByUserIdAndTournamentId(user.getId(), tournament.getId()))
                .willReturn(Optional.of(userTournamentGroup));

        // when
        // then
        assertThatThrownBy(() -> underTest.claimReward(tournament.getId(), user.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Reward already claimed");
    }

    @Test
    void willThrowWhenClaimRewardUserNotEligibleForReward() {
        // given
        User user = new User();
        user.setId(UUID.randomUUID());
        Date date = Date.from(Instant.now().minus(Duration.ofDays(1)));
        Tournament tournament = Tournament.builder().endDateTime(date).build();
        TournamentGroup group = new TournamentGroup(tournament);
        UserTournamentGroup userTournamentGroup = new UserTournamentGroup(user, group, 4);
        given(userTournamentGroupRepository.findByUserIdAndTournamentId(user.getId(), tournament.getId()))
                .willReturn(Optional.of(userTournamentGroup));

        // when
        // then
        assertThatThrownBy(() -> underTest.claimReward(tournament.getId(), user.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User is not eligible for a reward");
    }

    @Test
    void shouldInActiveTournament() {
        // given
        User user = new User(new Country("TR", "Turkey"));
        user.setId(UUID.randomUUID());
        Tournament tournament = new Tournament();
        TournamentGroup group = new TournamentGroup(tournament);
        group.setStartDate(Date.from(Instant.now()));
        UserTournamentGroup userTournamentGroup = new UserTournamentGroup(user, group, 1);

        given(tournamentRepository.findOngoingTournament(any(Date.class))).willReturn(Optional.of(tournament));
        given(userTournamentGroupRepository.findByUserIdAndTournamentId(user.getId(), tournament.getId()))
                .willReturn(Optional.of(userTournamentGroup));

        // when
        boolean expected = underTest.isInActiveTournament(user);

        // then
        assertThat(expected).isTrue();
    }

    @Test
    void shouldNotInActiveTournamentWhenGroupNotStarted() {
        // given
        User user = new User(new Country("TR", "Turkey"));
        user.setId(UUID.randomUUID());
        Tournament tournament = new Tournament();
        TournamentGroup group = new TournamentGroup(tournament);
        UserTournamentGroup userTournamentGroup = new UserTournamentGroup(user, group, 1);

        TournamentService spy = Mockito.spy(underTest);
        given(spy.getCurrentTournament()).willReturn(tournament);
        given(userTournamentGroupRepository.findByUserIdAndTournamentId(user.getId(), tournament.getId()))
                .willReturn(Optional.of(userTournamentGroup));

        // when
        boolean expected = spy.isInActiveTournament(user);

        // then
        assertThat(expected).isFalse();
    }

    @Test
    void shouldNotInActiveTournamentWhenNotInTournament() {
        // given
        User user = new User(new Country("TR", "Turkey"));
        user.setId(UUID.randomUUID());
        Tournament tournament = new Tournament();
        Long id = 1L;
        tournament.setId(id);

        TournamentService spy = Mockito.spy(underTest);
        given(spy.getCurrentTournament()).willReturn(tournament);
        given(userTournamentGroupRepository.findByUserIdAndTournamentId(user.getId(), id))
                .willReturn(Optional.empty());

        // when
        boolean expected = spy.isInActiveTournament(user);

        // then
        assertThat(expected).isFalse();
    }

    @Test
    void shouldNotInActiveTournamentWhenNoCurrentTournament() {
        // given
        User user = new User(new Country("TR", "Turkey"));
        user.setId(UUID.randomUUID());

        TournamentService spy = Mockito.spy(underTest);
        given(spy.getCurrentTournament()).willThrow(IllegalArgumentException.class);

        // when
        boolean expected = spy.isInActiveTournament(user);

        // then
        assertThat(expected).isFalse();
    }

    @Test
    void shouldEligibleForReward() {  // TODO: refactor when ranking buckets is implemented.
        // given
        int rank = 3;

        // when
        boolean expected = underTest.isEligibleForReward(rank);

        // then
        assertThat(expected).isTrue();
    }

    @Test
    void shouldNotEligibleForReward() {  // TODO: refactor when ranking buckets is implemented.
        // given
        int rank = 4;

        // when
        boolean expected = underTest.isEligibleForReward(rank);

        // then
        assertThat(expected).isFalse();
    }

    @Test
    void shouldReturnCurrentTournamentWhenPresent() {
        // given
        Tournament tournament = new Tournament();
        given(tournamentRepository.findOngoingTournament(any(Date.class))).willReturn(Optional.of(tournament));

        // when
        Tournament result = underTest.getCurrentTournament();

        // then
        assertThat(result).isEqualTo(tournament);
    }

    @Test
    void shouldCreateNewTournamentWhenNoCurrentTournamentAndWithinTournamentHours() {
        // given
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalTime time = LocalTime.of(TournamentService.DAILY_TOURNAMENT_END_HOUR - 1, 0);
        Instant mockedNow = ZonedDateTime.of(today, time, ZoneOffset.UTC).toInstant();

        try (MockedStatic<Instant> instantMock = Mockito.mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS)) {
            given(tournamentRepository.findOngoingTournament(any())).willReturn(Optional.empty());
            given(tournamentRepository.save(any(Tournament.class))).willReturn(new Tournament());
            instantMock.when(Instant::now).thenReturn(mockedNow);

            // when
            Tournament result = underTest.getCurrentTournament();

            // then
            verify(tournamentRepository).save(any(Tournament.class));
            assertThat(result).isNotNull();
        }
    }

    @Test
    void shouldThrowExceptionWhenNoCurrentTournamentAndOutsideTournamentHours() {
        // given
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalTime time = LocalTime.of(TournamentService.DAILY_TOURNAMENT_END_HOUR + 1, 0);
        Instant mockedNow = ZonedDateTime.of(today, time, ZoneOffset.UTC).toInstant();

        try (MockedStatic<Instant> instantMock = Mockito.mockStatic(Instant.class, Mockito.CALLS_REAL_METHODS)) {
            given(tournamentRepository.findOngoingTournament(any())).willReturn(Optional.empty());
            instantMock.when(Instant::now).thenReturn(mockedNow);

            // when
            // then
            assertThatThrownBy(() -> underTest.getCurrentTournament())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("No active tournament");
        }
    }

    @Test
    void updateUserLevel() {
        // given
        int userRanking = 2;
        int rivalRanking = 1;
        int score = 200;

        User user = new User();
        User anotherUser = new User();
        Tournament tournament = new Tournament();
        UserTournamentGroup users = new UserTournamentGroup(user, null, userRanking);
        UserTournamentGroup rivals = new UserTournamentGroup(anotherUser, null, rivalRanking);
        users.setScore(score);
        rivals.setScore(score);

        TournamentService spy = Mockito.spy(underTest);

        given(spy.getCurrentTournament()).willReturn(tournament);
        given(userTournamentGroupRepository.findByUserIdAndTournamentId(any(), any()))
                .willReturn(Optional.of(users));
        given(userTournamentGroupRepository.findByTournamentGroupAndRanking(any(), any(Integer.class)))
                .willReturn(Optional.of(rivals));

        // when
        spy.updateUserLevel(user);

        // then
        ArgumentCaptor<UserTournamentGroup> userTournamentGroupArgumentCaptor = ArgumentCaptor.forClass(UserTournamentGroup.class);
        verify(userTournamentGroupRepository, times(2)).save(userTournamentGroupArgumentCaptor.capture());
        List<UserTournamentGroup> capturedUserTournamentGroup = userTournamentGroupArgumentCaptor.getAllValues();
        rivals = capturedUserTournamentGroup.get(0);
        users = capturedUserTournamentGroup.get(1);
        assertThat(users.getRanking()).isEqualTo(userRanking - 1);
        assertThat(users.getScore()).isEqualTo(score + 1);
        assertThat(rivals.getRanking()).isEqualTo(rivalRanking + 1);
    }
}