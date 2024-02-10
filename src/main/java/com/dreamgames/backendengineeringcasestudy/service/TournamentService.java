package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.model.Tournament;
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
import java.util.UUID;

@Service
public class TournamentService {
    public final static int TOURNAMENT_GROUP_SIZE = 5;
    public final static int TOURNAMENT_LEVEL_REQUIREMENT = 20;
    public final static int TOURNAMENT_ENTRY_FEE = 1000;

    private final TournamentRepository tournamentRepository;
    private final UserTournamentGroupRepository userTournamentGroupRepository;
    private final UserRepository userRepository;

    public TournamentService(TournamentRepository tournamentRepository,
                             UserTournamentGroupRepository userTournamentGroupRepository,
                             UserRepository userRepository) {
        this.tournamentRepository = tournamentRepository;
        this.userTournamentGroupRepository = userTournamentGroupRepository;
        this.userRepository = userRepository;
    }

    public void enterTournament(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException(userId, User.class.getName()));
        List<UserTournamentGroup> unclaimedRewards = userTournamentGroupRepository
                .findUserTournamentGroupsByUserAndRankLessThanAndRewardClaimed(user, 3, false);

        if (user.getLevel() < TOURNAMENT_LEVEL_REQUIREMENT)
            throw new IllegalArgumentException("User level is not enough to enter the tournament");
        if (user.getCoins() < TOURNAMENT_ENTRY_FEE)
            throw new IllegalArgumentException("User does not have enough coins to enter the tournament");
        if (!unclaimedRewards.isEmpty())
            throw new IllegalArgumentException("User has unclaimed rewards");

        Tournament tournament = getCurrentTournament()
                .orElseThrow(() -> new ObjectNotFoundException(Tournament.class, "No ongoing tournament"));
        UserTournamentGroup userTournamentGroup = new UserTournamentGroup();
        userTournamentGroup.setUser(user);
        userTournamentGroup.setEnteredAt(Date.from(Instant.now()));
        userTournamentGroup.setTournamentGroup(tournament.getGroups().get(0));  // TODO: implement group selection
        userTournamentGroupRepository.save(userTournamentGroup);
    }

    public User claimReward(Long id, UUID userId) {
        UserTournamentGroup userTournamentGroup = userTournamentGroupRepository
                .findByUserIdAndTournamentId(userId, id)
                .orElseThrow(() -> new ObjectNotFoundException(userId, UserTournamentGroup.class.getName()));
        Tournament tournament = userTournamentGroup.getTournamentGroup().getTournament();
        User user = userTournamentGroup.getUser();

        if (userTournamentGroup.isRewardClaimed())
            throw new IllegalArgumentException("Reward already claimed");
        if (tournament.getEndDateTime().after(Date.from(Instant.now())))
            throw new IllegalArgumentException("Tournament has not ended yet");
        if (!isEligibleForReward(userTournamentGroup.getRank()))
            throw new IllegalArgumentException("User is not eligible for a reward");


        if (userTournamentGroup.getRank() == 1)  // TODO: implement reward calculation
            user.setCoins(user.getCoins() + 10000);
        else if (userTournamentGroup.getRank() == 2)
            user.setCoins(user.getCoins() + 5000);

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

    public boolean isEligibleForReward(int rank) {
        return rank <= 3;
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

        if (userTournamentGroup.getRank() > 1) {
            UserTournamentGroup rival = userTournamentGroupRepository.findByTournamentGroupAndRank(
                    userTournamentGroup.getTournamentGroup(),
                    userTournamentGroup.getRank() - 1);

            if (rival.getScore() < userTournamentGroup.getScore()) {
                userTournamentGroup.setRank(userTournamentGroup.getRank() - 1);
                rival.setRank(rival.getRank() + 1);
                userTournamentGroupRepository.save(rival);
            }
        }

        userTournamentGroup.setScore(userTournamentGroup.getScore() + 1);
        userTournamentGroupRepository.save(userTournamentGroup);
    }
}
