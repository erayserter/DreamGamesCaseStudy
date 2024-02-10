package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.dto.UserProgressResponse;
import com.dreamgames.backendengineeringcasestudy.dto.UserResponse;
import com.dreamgames.backendengineeringcasestudy.dto.UserResponseMapper;
import com.dreamgames.backendengineeringcasestudy.model.Country;
import com.dreamgames.backendengineeringcasestudy.model.User;
import com.dreamgames.backendengineeringcasestudy.repository.CountryRepository;
import com.dreamgames.backendengineeringcasestudy.repository.UserRepository;
import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

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
        System.out.println("entity: " + entity);
        User user = userRepository.save(entity);
        return userResponseMapper.apply(user);
    }

    public UserProgressResponse updateLevel(UUID userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException(userId, User.class.getName()));
        user.setLevel(user.getLevel() + 1);
        user.setCoins(user.getCoins() + 25);

        if (tournamentService.isInActiveTournament(user)) {
            tournamentService.updateUserLevel(user);
        }

        user = userRepository.save(user);
        return new UserProgressResponse(
                user.getLevel(),
                user.getCoins()
        );
    }
}
