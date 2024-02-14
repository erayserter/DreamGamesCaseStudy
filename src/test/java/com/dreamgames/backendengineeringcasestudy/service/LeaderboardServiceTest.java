package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.dto.CountryTournamentScoreResponse;
import com.dreamgames.backendengineeringcasestudy.dto.UserTournamentScoreResponseMapper;
import com.dreamgames.backendengineeringcasestudy.model.Tournament;
import com.dreamgames.backendengineeringcasestudy.model.TournamentGroup;
import com.dreamgames.backendengineeringcasestudy.model.User;
import com.dreamgames.backendengineeringcasestudy.model.UserTournamentGroup;
import com.dreamgames.backendengineeringcasestudy.repository.UserTournamentGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock private UserTournamentGroupRepository userTournamentGroupRepository;
    @Mock private TournamentService tournamentService;
    @Mock private UserTournamentScoreResponseMapper userTournamentScoreResponseMapper;

    private LeaderboardService underTest;

    private User user;
    private Tournament tournament;
    private TournamentGroup tournamentGroup;
    private UserTournamentGroup userTournamentGroup;
    private List<User> users;
    private List<UserTournamentGroup> userTournamentGroups;


    @BeforeEach
    void setUp() {
        underTest = new LeaderboardService(
                userTournamentGroupRepository,
                tournamentService,
                userTournamentScoreResponseMapper
        );

        tournament = Tournament.builder().id(1L).build();
        tournamentGroup = TournamentGroup.builder().id(1L).build();

        users = new ArrayList<>();
        userTournamentGroups = new ArrayList<>();

        for (int userIndex = 0; userIndex < TournamentService.TOURNAMENT_GROUP_SIZE; userIndex++) {
            User user = User.builder().id(UUID.randomUUID()).build();
            UserTournamentGroup userTournamentGroup =
                    new UserTournamentGroup(user, tournamentGroup, userIndex + 1);
            users.add(user);
            userTournamentGroups.add(userTournamentGroup);
        }

        user = users.get(0);
        userTournamentGroup = userTournamentGroups.get(0);
    }

    @Test
    void shouldGetGroupRank() {
        // given
        int rank = 1;
        UserTournamentGroup userTournamentGroup =
                new UserTournamentGroup(user, tournamentGroup, rank);

        given(userTournamentGroupRepository.findByUserIdAndTournamentId(any(UUID.class), any(Long.class)))
                .willReturn(Optional.of(userTournamentGroup));

        // when
        int expected = underTest.getGroupRank(
                user.getId(),
                tournament.getId()
        );

        // then
        assertThat(rank).isEqualTo(expected);
    }

    @Test
    void willThrowWhenGetGroupRankIfNotInTournament() {
        // given
        given(userTournamentGroupRepository.findByUserIdAndTournamentId(any(UUID.class), any(Long.class)))
                .willReturn(Optional.empty());

        // when
        // then
        assertThatThrownBy(() -> underTest.getGroupRank(user.getId(), tournament.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("This user is not attended to this tournament");
    }

    @Test
    void shouldGetGroupLeaderboard() {
        // given
        given(tournamentService.getCurrentTournament()).willReturn(tournament);
        given(userTournamentGroupRepository.findByUserIdAndTournamentId(any(UUID.class), any(Long.class)))
                .willReturn(Optional.of(userTournamentGroup));
        given(userTournamentGroupRepository.orderGroupByScores(any()))
                .willReturn(userTournamentGroups);

        // when
        var expected = underTest.getGroupLeaderboard(user.getId());

        // then
        assertThat(userTournamentGroups.size()).isEqualTo(expected.size());
    }

    @Test
    void shouldGetCountryLeaderboard() {
        // given
        ArgumentCaptor<Long> tournamentIdCaptor = ArgumentCaptor.forClass(Long.class);
        given(tournamentService.getCurrentTournament()).willReturn(tournament);
        given(userTournamentGroupRepository.findCountryScoresByTournamentId(any(Long.class)))
                .willReturn(List.of(new CountryTournamentScoreResponse("Turkey", 2000L), new CountryTournamentScoreResponse("The United States", 1000L)));

        // when
        var expected = underTest.getCountryLeaderboard();

        // then
        verify(userTournamentGroupRepository).findCountryScoresByTournamentId(tournamentIdCaptor.capture());
        assertThat(tournament.getId()).isEqualTo(tournamentIdCaptor.getValue());
        assertThat(2).isEqualTo(expected.size());
    }
}