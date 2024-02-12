package com.dreamgames.backendengineeringcasestudy.dto;

import com.dreamgames.backendengineeringcasestudy.model.UserTournamentGroup;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class UserTournamentScoreResponseMapper implements Function<UserTournamentGroup, UserTournamentScoreResponse> {
    @Override
    public UserTournamentScoreResponse apply(UserTournamentGroup userTournamentGroup) {
        return new UserTournamentScoreResponse(
                userTournamentGroup.getUser().getId(),
                userTournamentGroup.getUser().getCountry().getName(),
                userTournamentGroup.getScore()
        );
    }

}
