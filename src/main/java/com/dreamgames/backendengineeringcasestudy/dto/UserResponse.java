package com.dreamgames.backendengineeringcasestudy.dto;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String country_name,
        int level,
        int coins
) {
}
