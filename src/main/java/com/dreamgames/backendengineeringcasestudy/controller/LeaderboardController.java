package com.dreamgames.backendengineeringcasestudy.controller;

import com.dreamgames.backendengineeringcasestudy.dto.CountryTournamentScoreResponse;
import com.dreamgames.backendengineeringcasestudy.dto.UserTournamentScoreResponse;
import com.dreamgames.backendengineeringcasestudy.service.LeaderboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/group-rank")
    int getGroupRank(@RequestParam(name = "user_id") String userIdString,
                     @RequestParam(name = "tournament_id") Long tournamentId) {
        UUID userId = UUID.fromString(userIdString);
        return leaderboardService.getGroupRank(userId, tournamentId);
    }

    @GetMapping("/group-leaderboard")
    List<UserTournamentScoreResponse> getGroupLeaderboard(@RequestParam(name = "user_id") String userIdString) {
        UUID userId = UUID.fromString(userIdString);
        return leaderboardService.getGroupLeaderboard(userId);
    }

    @GetMapping("/country-leaderboard")
    List<CountryTournamentScoreResponse> getCountryLeaderboard() {
        return leaderboardService.getCountryLeaderboard();
    }
}
