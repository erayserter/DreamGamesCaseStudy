package com.dreamgames.backendengineeringcasestudy.controller;

import com.dreamgames.backendengineeringcasestudy.dto.SuccessResponse;
import com.dreamgames.backendengineeringcasestudy.dto.UserRequest;
import com.dreamgames.backendengineeringcasestudy.model.User;
import com.dreamgames.backendengineeringcasestudy.service.TournamentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tournaments")
public class TournamentController {
    private final TournamentService tournamentService;

    public TournamentController(TournamentService tournamentService) {
        this.tournamentService = tournamentService;
    }

    @PostMapping("/enter")
    public ResponseEntity<?> enterTournament(@RequestBody UserRequest userRequest) {
        UUID userId = userRequest.userId();
        tournamentService.enterTournament(userId);
        return ResponseEntity.ok().body(new SuccessResponse("User successfully entered the tournament"));
    }

    @PostMapping("{id}/claim-reward")
    public User claimReward(@PathVariable Long id, @RequestBody UserRequest userRequest) {
        UUID userId = userRequest.userId();
        return tournamentService.claimReward(id, userId);
    }
}
