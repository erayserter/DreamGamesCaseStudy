package com.dreamgames.backendengineeringcasestudy.dto;

import java.util.UUID;

public record UserTournamentScoreResponse(
        UUID userId,
        String country,
        int score
) {
}
