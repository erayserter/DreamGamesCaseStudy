package com.dreamgames.backendengineeringcasestudy.controller;

import com.dreamgames.backendengineeringcasestudy.model.User;
import com.dreamgames.backendengineeringcasestudy.service.TournamentService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/tournaments")
public class TournamentController {
    private final TournamentService tournamentService;


    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @PostMapping("/enter")
    public void enterTournament() {
        tournamentService.enterTournament();
    }

    @PostMapping("{id}/claim-reward")
    public User claimReward(@PathVariable int id) {
        return tournamentService.claimReward(id);
    }
}
