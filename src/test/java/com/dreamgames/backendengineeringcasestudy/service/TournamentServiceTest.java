package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.exception.BadRequestException;
import com.dreamgames.backendengineeringcasestudy.exception.EntityNotFoundException;
import com.dreamgames.backendengineeringcasestudy.model.*;
import com.dreamgames.backendengineeringcasestudy.repository.*;
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
    @Mock private RewardBucketRepository rewardBucketRepository;
    @Mock private UserRepository userRepository;
    private TournamentService underTest;

    private User user;
    private Tournament tournament;
    private TournamentGroup tournamentGroup;
    private UserTournamentGroup userTournamentGroup;

    @BeforeEach
    void setUp() {
        underTest = new TournamentService(
                tournamentRepository,
                tournamentGroupRepository,
                userTournamentGroupRepository,
                rewardBucketRepository,
                userRepository
        );

        Country country = new Country("TR", "Turkey");
        user = User.builder().id(UUID.randomUUID()).country(country).level(20).coins(1000).build();
        tournament = Tournament.builder().id(1L).entryFee(1000).groupSizes(5).levelRequirement(20).build();
        tournamentGroup = new TournamentGroup(tournament);
        tournament.setGroups(List.of(tournamentGroup));
        userTournamentGroup = new UserTournamentGroup(user, tournamentGroup);
    }

    @Test
    void shouldEnterTournamentEmptyGroup() {
        // given
        TournamentService spy = Mockito.spy(underTest);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userTournamentGroupRepository
                .findPreviousUnclaimedTournamentRewards(
                        any(UUID.class),
                        any(Date.class)
                ))
                .willReturn(List.of());
        doReturn(tournament).when(spy).getCurrentTournament();
        given(userTournamentGroupRepository
                .findByUserIdAndTournamentId(user.getId(), tournament.getId()))
                .willReturn(Optional.empty());
        given(tournamentGroupRepository
                .findByTournamentAndUserTournamentGroups_User_CountryNot(tournament, user.getCountry()))
                .willReturn(Optional.empty());
        given(tournamentGroupRepository.save(any(TournamentGroup.class))).willReturn(tournamentGroup);

        // when
        spy.enterTournament(user.getId());

        // then
        ArgumentCaptor<UserTournamentGroup> userTournamentGroupArgumentCaptor = ArgumentCaptor.forClass(UserTournamentGroup.class);
        verify(userTournamentGroupRepository).save(userTournamentGroupArgumentCaptor.capture());
        UserTournamentGroup capturedUserTournamentGroup = userTournamentGroupArgumentCaptor.getValue();
        assertThat(capturedUserTournamentGroup.getUser()).isEqualTo(user);
        assertThat(capturedUserTournamentGroup.getTournamentGroup()).isEqualTo(tournamentGroup);
    }

    @Test
    void shouldEnterTournamentLastAttendee() {
        // given
        List<UserTournamentGroup> users = new ArrayList<>();
        for (int i = 1; i < tournament.getGroupSizes(); i++) {
            users.add(new UserTournamentGroup(null, tournamentGroup));
        }
        tournamentGroup.setUserTournamentGroups(users);

        TournamentService spy = Mockito.spy(underTest);

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userTournamentGroupRepository
                .findPreviousUnclaimedTournamentRewards(
                        any(UUID.class),
                        any(Date.class)
                ))
                .willReturn(List.of());
        doReturn(tournament).when(spy).getCurrentTournament();
        given(userTournamentGroupRepository
                .findByUserIdAndTournamentId(user.getId(), tournament.getId()))
                .willReturn(Optional.empty());
        given(tournamentGroupRepository
                .findByTournamentAndUserTournamentGroups_User_CountryNot(tournament, user.getCountry()))
                .willReturn(Optional.of(tournamentGroup));
        given(tournamentGroupRepository.save(any(TournamentGroup.class))).willReturn(tournamentGroup);

        // when
        spy.enterTournament(user.getId());

        // then
        ArgumentCaptor<UserTournamentGroup> userTournamentGroupArgumentCaptor = ArgumentCaptor.forClass(UserTournamentGroup.class);
        verify(userTournamentGroupRepository).save(userTournamentGroupArgumentCaptor.capture());
        UserTournamentGroup capturedUserTournamentGroup = userTournamentGroupArgumentCaptor.getValue();
        assertThat(capturedUserTournamentGroup.getUser()).isEqualTo(user);
        assertThat(capturedUserTournamentGroup.getTournamentGroup()).isEqualTo(tournamentGroup);
    }

    @Test
    void willThrowWhenEnterTournamentUserNotFound() {
        // given
        given(userRepository.findById(user.getId())).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> underTest.enterTournament(user.getId()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void willThrowWhenEnterTournamentUserLevelNotEnough() {
        // given
        TournamentService spy = Mockito.spy(underTest);
        user.setLevel(tournament.getLevelRequirement() - 1);

        given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(user));
        doReturn(tournament).when(spy).getCurrentTournament();

        // when
        // then
        assertThatThrownBy(() -> spy.enterTournament(user.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User level is not enough to enter the tournament");
    }

    @Test
    void willThrowWhenEnterTournamentUserCoinsNotEnough() {
        // given
        user.setCoins(tournament.getEntryFee() - 1);
        TournamentService spy = Mockito.spy(underTest);

        given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(user));
        doReturn(tournament).when(spy).getCurrentTournament();

        // when
        // then
        assertThatThrownBy(() -> spy.enterTournament(user.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User does not have enough coins to enter the tournament");
    }

    @Test
    void willThrowWhenEnterTournamentUserHasUnclaimedRewards() {
        // given
        TournamentService spy = Mockito.spy(underTest);

        given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(user));
        doReturn(tournament).when(spy).getCurrentTournament();
        given(userTournamentGroupRepository
                .findPreviousUnclaimedTournamentRewards(
                        any(UUID.class),
                        any(Date.class)
                ))
                .willReturn(List.of(userTournamentGroup));

        // when
        // then
        assertThatThrownBy(() -> spy.enterTournament(UUID.randomUUID()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User has unclaimed rewards");
    }

    @Test
    void willThrowWhenEnterTournamentUserAlreadyEntered() {
        // given
        TournamentService spy = Mockito.spy(underTest);

        given(userRepository.findById(any(UUID.class))).willReturn(Optional.of(user));
        given(userTournamentGroupRepository
                .findPreviousUnclaimedTournamentRewards(
                        any(UUID.class),
                        any(Date.class)
                ))
                .willReturn(List.of());
        doReturn(new Tournament()).when(spy).getCurrentTournament();
        given(userTournamentGroupRepository
                .findByUserIdAndTournamentId(any(UUID.class), any()))
                .willReturn(Optional.of(new UserTournamentGroup()));

        // when
        // then
        assertThatThrownBy(() -> spy.enterTournament(UUID.randomUUID()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User has already entered the tournament");
    }

    @Test
    void updateUserLevel() {
        // given
        int score = 200;
        userTournamentGroup.setScore(score);

        TournamentService spy = Mockito.spy(underTest);

        doReturn(tournament).when(spy).getCurrentTournament();
        given(userTournamentGroupRepository.findByUserIdAndTournamentId(any(), any()))
                .willReturn(Optional.of(userTournamentGroup));

        // when
        spy.updateUserLevel(user);

        // then
        ArgumentCaptor<UserTournamentGroup> userTournamentGroupArgumentCaptor = ArgumentCaptor.forClass(UserTournamentGroup.class);
        verify(userTournamentGroupRepository).save(userTournamentGroupArgumentCaptor.capture());
        UserTournamentGroup capturedUserTournamentGroup = userTournamentGroupArgumentCaptor.getValue();
        assertThat(capturedUserTournamentGroup.getScore()).isEqualTo(score + 1);
    }

    @Test
    void willThrowWhenUpdateUserLevelUserNotInTournament() {
        // given
        TournamentService spy = Mockito.spy(underTest);

        doReturn(tournament).when(spy).getCurrentTournament();
        given(userTournamentGroupRepository.findByUserIdAndTournamentId(any(UUID.class), any(Long.class)))
                .willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> spy.updateUserLevel(user))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void shouldClaimReward() {
        // given
        int coins = 5000;
        int reward = 10000;
        user.setCoins(coins);
        Date date = Date.from(Instant.now().minus(Duration.ofDays(1)));
        tournament.setEndDateTime(date);

        TournamentService spy = Mockito.spy(underTest);

        given(userTournamentGroupRepository.findByUserIdAndTournamentId(user.getId(), tournament.getId()))
                .willReturn(Optional.of(userTournamentGroup));
        doReturn(reward).when(spy).calculateReward(any(UserTournamentGroup.class));

        // when
        spy.claimReward(tournament.getId(), user.getId());

        // then
        ArgumentCaptor<UserTournamentGroup> userTournamentGroupArgumentCaptor = ArgumentCaptor.forClass(UserTournamentGroup.class);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userTournamentGroupRepository).save(userTournamentGroupArgumentCaptor.capture());
        verify(userRepository).save(userArgumentCaptor.capture());
        UserTournamentGroup capturedUserTournamentGroup = userTournamentGroupArgumentCaptor.getValue();
        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUserTournamentGroup.isRewardClaimed()).isTrue();
        assertThat(capturedUser.getCoins()).isEqualTo(coins + reward);
    }

    @Test
    void willThrowWhenClaimRewardUserTournamentNotFound() {
        // given
        given(userTournamentGroupRepository.findByUserIdAndTournamentId(any(UUID.class), any(Long.class)))
                .willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> underTest.claimReward(1L, user.getId()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void willThrowWhenClaimRewardTournamentNotEnded() {
        // given
        Date date = Date.from(Instant.now().plus(Duration.ofDays(1)));
        tournament.setEndDateTime(date);
        given(userTournamentGroupRepository.findByUserIdAndTournamentId(user.getId(), tournament.getId()))
                .willReturn(Optional.of(new UserTournamentGroup(user, tournamentGroup)));

        // when
        // then
        assertThatThrownBy(() -> underTest.claimReward(tournament.getId(), user.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Tournament has not ended yet");
    }

    @Test
    void willThrowWhenClaimRewardRewardAlreadyClaimed() {
        // given
        Date date = Date.from(Instant.now().minus(Duration.ofDays(1)));
        tournament.setEndDateTime(date);
        userTournamentGroup.setRewardClaimed(true);
        given(userTournamentGroupRepository.findByUserIdAndTournamentId(user.getId(), tournament.getId()))
                .willReturn(Optional.of(userTournamentGroup));

        // when
        // then
        assertThatThrownBy(() -> underTest.claimReward(tournament.getId(), user.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Reward already claimed");
    }

    @Test
    void shouldCalculateReward() {
        // given
        int rank = 1;
        int reward = 10000;

        List<UserTournamentGroup> scores = new ArrayList<>();
        scores.add(userTournamentGroup);
        for (int i = 1; i < tournament.getGroupSizes(); i++) {
            scores.add(new UserTournamentGroup(null, tournamentGroup));
        }
        RewardBucket rewardBucket = RewardBucket.builder().startRank(rank).endRank(rank).rewardAmount(reward).build();

        given(userTournamentGroupRepository.orderGroupByScores(tournamentGroup.getId())).willReturn(scores);
        given(rewardBucketRepository.findRewardBucketByRank(tournament.getId(), rank)).willReturn(Optional.of(rewardBucket));

        // when
        int result = underTest.calculateReward(userTournamentGroup);

        // then
        assertThat(result).isEqualTo(reward);
    }

    @Test
    void willThrowWhenCalculateRewardUserNotEligible() {
        // given
        List<UserTournamentGroup> scores = new ArrayList<>();
        for (int i = 1; i < tournament.getGroupSizes(); i++) {
            scores.add(new UserTournamentGroup(null, tournamentGroup));
        }
        given(userTournamentGroupRepository.orderGroupByScores(tournamentGroup.getId())).willReturn(scores);
        given(rewardBucketRepository.findRewardBucketByRank(any(Long.class), any(Integer.class))).willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> underTest.calculateReward(userTournamentGroup))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User is not eligible for a reward");
    }

    @Test
    void shouldInActiveTournament() {
        // given
        tournamentGroup.setStartDate(Date.from(Instant.now()));

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
        TournamentService spy = Mockito.spy(underTest);

        doReturn(tournament).when(spy).getCurrentTournament();
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
        TournamentService spy = Mockito.spy(underTest);

        doReturn(tournament).when(spy).getCurrentTournament();
        given(userTournamentGroupRepository.findByUserIdAndTournamentId(user.getId(), tournament.getId()))
                .willReturn(Optional.empty());

        // when
        boolean expected = spy.isInActiveTournament(user);

        // then
        assertThat(expected).isFalse();
    }

    @Test
    void shouldNotInActiveTournamentWhenNoCurrentTournament() {
        // given
        TournamentService spy = Mockito.spy(underTest);

        doThrow(BadRequestException.class).when(spy).getCurrentTournament();

        // when
        boolean expected = spy.isInActiveTournament(user);

        // then
        assertThat(expected).isFalse();
    }

    @Test
    void shouldGetCurrentTournamentWhenPresent() {
        // given
        given(tournamentRepository.findOngoingTournament(any(Date.class))).willReturn(Optional.of(tournament));

        // when
        Tournament result = underTest.getCurrentTournament();

        // then
        assertThat(result).isEqualTo(tournament);
    }

    @Test
    void shouldThrowWhenGetCurrentTournamentOutsideTournamentHours() {
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
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("No active tournament");
        }
    }

    @Test
    void shouldCreateDailyTournament() {
        // given
        given(tournamentRepository.findOngoingTournament(any(Date.class))).willReturn(Optional.empty());

        // when
        underTest.createDailyTournament();

        // then
        verify(tournamentRepository).save(any(Tournament.class));
    }

    @Test
    void shouldNotCreateDailyTournamentWhenAlreadyPresent() {
        // given
        given(tournamentRepository.findOngoingTournament(any(Date.class))).willReturn(Optional.of(tournament));

        // when
        underTest.createDailyTournament();

        // then
        verify(tournamentRepository, never()).save(any(Tournament.class));
    }

    @Test
    void shouldCleanupDailyTournament() {
        // given
        TournamentService spy = Mockito.spy(underTest);

        tournamentGroup.setStartDate(Date.from(Instant.now()));
        List<UserTournamentGroup> scores = List.of(userTournamentGroup);

        doReturn(tournament).when(spy).getCurrentTournament();
        given(userTournamentGroupRepository.orderGroupByScores(tournamentGroup.getId())).willReturn(scores);
        given(rewardBucketRepository.findRewardBucketByRank(any(Long.class), any(Integer.class)))
                .willReturn(Optional.of(new RewardBucket()));

        // when
        spy.cleanupDailyTournament();

        // then
        verify(userTournamentGroupRepository).saveAll(scores);
        assertThat(userTournamentGroup.isHasReward()).isTrue();
    }

    @Test
    void shouldCleanupDailyTournamentDoNothingWhenNoRewards() {
        // given
        TournamentService spy = Mockito.spy(underTest);

        tournamentGroup.setStartDate(Date.from(Instant.now()));
        List<UserTournamentGroup> scores = List.of(userTournamentGroup);

        doReturn(tournament).when(spy).getCurrentTournament();
        given(userTournamentGroupRepository.orderGroupByScores(tournamentGroup.getId())).willReturn(scores);
        given(rewardBucketRepository.findRewardBucketByRank(any(Long.class), any(Integer.class)))
                .willReturn(Optional.empty());

        // when
        spy.cleanupDailyTournament();

        // then
        verify(userTournamentGroupRepository).saveAll(scores);
        assertThat(userTournamentGroup.isHasReward()).isFalse();
    }

    @Test
    void shouldCleanupDailyTournamentDoNothingWhenGroupNotStarted() {
        // given
        TournamentService spy = Mockito.spy(underTest);

        List<UserTournamentGroup> scores = List.of(userTournamentGroup);

        doReturn(tournament).when(spy).getCurrentTournament();

        // when
        spy.cleanupDailyTournament();

        // then
        verify(userTournamentGroupRepository, never()).saveAll(scores);
        assertThat(userTournamentGroup.isHasReward()).isFalse();
    }
}