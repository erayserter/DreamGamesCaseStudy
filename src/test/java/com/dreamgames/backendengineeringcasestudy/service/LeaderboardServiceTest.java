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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @BeforeEach
    void setUp() {
        underTest = new LeaderboardService(
                userTournamentGroupRepository,
                tournamentService,
                userTournamentScoreResponseMapper
        );
    }

    @Test
    void shouldGetGroupRank() {
        // given
        int rank = 100;
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        Long tournamentId = 1L;
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        TournamentGroup tournamentGroup = new TournamentGroup(tournament);
        UserTournamentGroup userTournamentGroup = new UserTournamentGroup(user, tournamentGroup, rank);

        given(userTournamentGroupRepository.findByUserIdAndTournamentId(any(UUID.class), any(Long.class)))
                .willReturn(Optional.of(userTournamentGroup));

        // when
        int expected = underTest.getGroupRank(userId, tournamentId);

        // then
        assertEquals(rank, expected);
    }

    @Test
    void willThrowWhenGetGroupRankIfNotInTournament() {
        // given
        UUID userId = UUID.randomUUID();
        Long tournamentId = 1L;

        given(userTournamentGroupRepository.findByUserIdAndTournamentId(any(UUID.class), any(Long.class)))
                .willReturn(Optional.empty());

        // when
        // then
        assertThrows(IllegalArgumentException.class, () -> underTest.getGroupRank(userId, tournamentId));
    }

    @Test
    void shouldGetGroupLeaderboard() {
        // given
        UUID userId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();
        User user = new User();
        User anotherUser = new User();
        user.setId(userId);
        anotherUser.setId(anotherUserId);
        Long tournamentId = 1L;
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);
        TournamentGroup tournamentGroup = new TournamentGroup(tournament);
        UserTournamentGroup userTournamentGroup = new UserTournamentGroup(user, tournamentGroup, 100);
        UserTournamentGroup userTournamentGroup2 = new UserTournamentGroup(anotherUser, tournamentGroup, 200);

        given(tournamentService.getCurrentTournament()).willReturn(tournament);
        given(userTournamentGroupRepository.findByUserIdAndTournamentId(any(UUID.class), any(Long.class)))
                .willReturn(Optional.of(userTournamentGroup));
        given(userTournamentGroupRepository.orderGroupByScores(any()))
                .willReturn(List.of(userTournamentGroup, userTournamentGroup2));

        // when
        var expected = underTest.getGroupLeaderboard(userId);

        // then
        assertEquals(2, expected.size());
    }

    @Test
    void shouldGetCountryLeaderboard() {
        // given
        Long tournamentId = 1L;
        Tournament tournament = new Tournament();
        tournament.setId(tournamentId);

        ArgumentCaptor<Long> tournamentIdCaptor = ArgumentCaptor.forClass(Long.class);
        given(tournamentService.getCurrentTournament()).willReturn(tournament);
        given(userTournamentGroupRepository.findCountryScoresByTournamentId(any(Long.class)))
                .willReturn(List.of(new CountryTournamentScoreResponse("Turkey", 2000L), new CountryTournamentScoreResponse("The United States", 1000L)));

        // when
        var expected = underTest.getCountryLeaderboard();

        // then
        verify(userTournamentGroupRepository).findCountryScoresByTournamentId(tournamentIdCaptor.capture());
        assertEquals(tournamentId, tournamentIdCaptor.getValue());
        assertEquals(2, expected.size());
    }
}