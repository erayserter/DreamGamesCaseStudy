package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.dto.UserProgressResponse;
import com.dreamgames.backendengineeringcasestudy.dto.UserResponse;
import com.dreamgames.backendengineeringcasestudy.dto.UserResponseMapper;
import com.dreamgames.backendengineeringcasestudy.exception.EntityNotFoundException;
import com.dreamgames.backendengineeringcasestudy.model.Country;
import com.dreamgames.backendengineeringcasestudy.model.User;
import com.dreamgames.backendengineeringcasestudy.repository.CountryRepository;
import com.dreamgames.backendengineeringcasestudy.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    public final static int LEVEL_UP_REWARD = 25;

    private final UserRepository userRepository;
    private final CountryRepository countryRepository;
    private final TournamentService tournamentService;
    private final UserResponseMapper userResponseMapper;

    public UserService(UserRepository userRepository,
                       CountryRepository countryRepository,
                       TournamentService tournamentService,
                       UserResponseMapper userResponseMapper) {
        this.userRepository = userRepository;
        this.countryRepository = countryRepository;
        this.tournamentService = tournamentService;
        this.userResponseMapper = userResponseMapper;
    }

    public UserResponse create() {
        Country country = countryRepository.getRandomCountry();
        User entity = new User(country);
        User user = userRepository.save(entity);
        return userResponseMapper.apply(user);
    }

    public UserProgressResponse updateLevel(UUID userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        user.setLevel(user.getLevel() + 1);
        user.setCoins(user.getCoins() + LEVEL_UP_REWARD);

        if (tournamentService.isInActiveTournament(user)) {
            tournamentService.updateUserLevel(user);
        }

        userRepository.save(user);
        return new UserProgressResponse(
                user.getLevel(),
                user.getCoins()
        );
    }
}
