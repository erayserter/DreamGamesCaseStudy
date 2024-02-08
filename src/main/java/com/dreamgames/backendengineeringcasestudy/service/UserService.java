package com.dreamgames.backendengineeringcasestudy.service;

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

    public UserService(UserRepository userRepository,
                       CountryRepository countryRepository,
                       TournamentService tournamentService) {
        this.userRepository = userRepository;
        this.countryRepository = countryRepository;
        this.tournamentService = tournamentService;
    }

    public User create() {
        Country country = countryRepository.getRandomCountry();
        User user = new User(country);
        return userRepository.save(user);
    }

    public void updateLevel(UUID userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException(userId, User.class.getName()));
        user.setLevel(user.getLevel() + 1);

        if (tournamentService.isInTournament(user)) {
            tournamentService.updateUserLevel(user);
        }

        userRepository.save(user);
    }
}
