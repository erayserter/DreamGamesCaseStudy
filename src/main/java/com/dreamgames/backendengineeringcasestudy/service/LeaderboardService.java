package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.dto.*;
import com.dreamgames.backendengineeringcasestudy.model.Tournament;
import com.dreamgames.backendengineeringcasestudy.model.UserTournamentGroup;
import com.dreamgames.backendengineeringcasestudy.repository.UserTournamentGroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LeaderboardService {

    private final UserTournamentGroupRepository userTournamentGroupRepository;
    private final TournamentService tournamentService;
    private final UserTournamentScoreResponseMapper userTournamentScoreResponseMapper;

    public LeaderboardService(UserTournamentGroupRepository userTournamentGroupRepository,
                              TournamentService tournamentService,
                              UserTournamentScoreResponseMapper userTournamentScoreResponseMapper) {
        this.userTournamentGroupRepository = userTournamentGroupRepository;
        this.tournamentService = tournamentService;
        this.userTournamentScoreResponseMapper = userTournamentScoreResponseMapper;
    }

    public int getGroupRank(UUID userId, Long tournamentId) {
        UserTournamentGroup userTournamentGroup =
                userTournamentGroupRepository
                        .findByUserIdAndTournamentId(userId, tournamentId)
                        .orElseThrow(() -> new IllegalArgumentException("This user is not attended to this tournament"));

        return userTournamentGroup.getRanking();
    }

    public List<UserTournamentScoreResponse> getGroupLeaderboard(UUID userId) {
        Tournament tournament = tournamentService.getCurrentTournament();

        UserTournamentGroup userTournamentGroup =
                userTournamentGroupRepository
                        .findByUserIdAndTournamentId(userId, tournament.getId())
                        .orElseThrow(() -> new IllegalArgumentException("This user is not attended to this tournament"));

        List<UserTournamentGroup> scores = userTournamentGroupRepository
                .orderGroupByScores(userTournamentGroup.getTournamentGroup().getId());

        return scores
                .stream()
                .map(userTournamentScoreResponseMapper)
                .toList();
    }

    public List<CountryTournamentScoreResponse> getCountryLeaderboard() {
        Tournament currentTournament = tournamentService.getCurrentTournament();

        return userTournamentGroupRepository.findCountryScoresByTournamentId(currentTournament.getId());
    }
}
