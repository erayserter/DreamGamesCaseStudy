package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.model.Tournament;
import com.dreamgames.backendengineeringcasestudy.model.TournamentGroup;
import com.dreamgames.backendengineeringcasestudy.model.User;
import com.dreamgames.backendengineeringcasestudy.model.UserTournamentGroup;
import com.dreamgames.backendengineeringcasestudy.repository.TournamentRepository;
import com.dreamgames.backendengineeringcasestudy.repository.UserRepository;
import com.dreamgames.backendengineeringcasestudy.repository.UserTournamentGroupRepository;
import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TournamentService {
    public final static int TOURNAMENT_GROUP_SIZE = 5;
    public final static int TOURNAMENT_LEVEL_REQUIREMENT = 20;
    public final static int TOURNAMENT_ENTRY_FEE = 1000;

    private final TournamentRepository tournamentRepository;
    private final UserTournamentGroupRepository userTournamentGroupRepository;
    private final UserRepository userRepository;

    public TournamentService(TournamentRepository tournamentRepository, UserTournamentGroupRepository userTournamentGroupRepository,
                             UserRepository userRepository) {
        this.tournamentRepository = tournamentRepository;
        this.userTournamentGroupRepository = userTournamentGroupRepository;
        this.userRepository = userRepository;
    }

    public void enterTournament(User user) {
        if (user.getLevel() < TOURNAMENT_LEVEL_REQUIREMENT) {
            throw new IllegalArgumentException("User level is not enough to enter the tournament");
        }
        if (user.getCoins() < TOURNAMENT_ENTRY_FEE) {
            throw new IllegalArgumentException("User does not have enough coins to enter the tournament");
        }
        // TODO: implement has unclaimed reward check

        Tournament tournament = getCurrentTournament()
                .orElseThrow(() -> new ObjectNotFoundException(Tournament.class, "No ongoing tournament"));
        UserTournamentGroup userTournamentGroup = new UserTournamentGroup();
        userTournamentGroup.setUser(user);
        userTournamentGroup.setEnteredAt(Date.from(Instant.now()));
        userTournamentGroup.setTournamentGroup(tournament.getGroups().get(0));  // TODO: implement group selection
        userTournamentGroupRepository.save(userTournamentGroup);
    }

    public User claimReward(User user, Long id) {
        UserTournamentGroup userTournamentGroup = userTournamentGroupRepository
                .findByUserIdAndTournamentId(user.getId(), id)
                .orElseThrow(() -> new ObjectNotFoundException(user.getId(), UserTournamentGroup.class.getName()));

        if (userTournamentGroup.isRewardClaimed()) {
            throw new IllegalArgumentException("Reward already claimed");
        }

        TournamentGroup tournamentGroup = userTournamentGroup.getTournamentGroup();
        List<UserTournamentGroup> utgs = tournamentGroup.getUserTournamentGroups();
        utgs = utgs.stream().sorted((utg1, utg2) -> utg2.getScore() - utg1.getScore()).toList();
        int rank = utgs.indexOf(userTournamentGroup);

        if (rank == 1)  // TODO: implement reward calculation
            user.setCoins(user.getCoins() + 10000);
        else if (rank == 2)
            user.setCoins(user.getCoins() + 5000);
        else
            throw new IllegalArgumentException("User did not win a reward");

        userTournamentGroup.setRewardClaimed(true);
        userTournamentGroupRepository.save(userTournamentGroup);
        userRepository.save(user);
        return user;
    }

    public boolean isInTournament(User user) {
        Tournament tournament = getCurrentTournament().orElse(null);
        if (tournament == null) {
            return false;
        }
        return userTournamentGroupRepository.findByUserIdAndTournamentId(user.getId(), tournament.getId()).isPresent();
    }

    public Optional<Tournament> getCurrentTournament() {
        Instant now = Instant.now();
        Date dateNow = Date.from(now);
        return tournamentRepository.findOngoingTournament(dateNow);
    }

    public void updateUserLevel(User user) {
        Tournament tournament = getCurrentTournament()
                .orElseThrow(() -> new ObjectNotFoundException(Tournament.class, "No ongoing tournament"));
        UserTournamentGroup userTournamentGroup = userTournamentGroupRepository
                .findByUserIdAndTournamentId(user.getId(), tournament.getId())
                .orElseThrow(() -> new ObjectNotFoundException(user.getId(), UserTournamentGroup.class.getName()));
        userTournamentGroup.setScore(userTournamentGroup.getScore() + 1);
        userTournamentGroupRepository.save(userTournamentGroup);
    }
}
