package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.model.Tournament;
import com.dreamgames.backendengineeringcasestudy.model.TournamentGroup;
import com.dreamgames.backendengineeringcasestudy.model.User;
import com.dreamgames.backendengineeringcasestudy.model.UserTournamentGroup;
import com.dreamgames.backendengineeringcasestudy.repository.TournamentGroupRepository;
import com.dreamgames.backendengineeringcasestudy.repository.TournamentRepository;
import com.dreamgames.backendengineeringcasestudy.repository.UserRepository;
import com.dreamgames.backendengineeringcasestudy.repository.UserTournamentGroupRepository;
import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TournamentService {
    public final static int TOURNAMENT_GROUP_SIZE = 5;
    public final static int TOURNAMENT_LEVEL_REQUIREMENT = 20;
    public final static int TOURNAMENT_ENTRY_FEE = 1000;
    public final static int DAILY_TOURNAMENT_START_HOUR = 0;
    public final static int DAILY_TOURNAMENT_END_HOUR = 20;

    private final TournamentRepository tournamentRepository;
    private final TournamentGroupRepository tournamentGroupRepository;
    private final UserTournamentGroupRepository userTournamentGroupRepository;
    private final UserRepository userRepository;

    public TournamentService(TournamentRepository tournamentRepository,
                             TournamentGroupRepository tournamentGroupRepository,
                             UserTournamentGroupRepository userTournamentGroupRepository,
                             UserRepository userRepository) {
        this.tournamentRepository = tournamentRepository;
        this.tournamentGroupRepository = tournamentGroupRepository;
        this.userTournamentGroupRepository = userTournamentGroupRepository;
        this.userRepository = userRepository;
    }

    public void enterTournament(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException(userId, User.class.getName()));

        if (user.getLevel() < TOURNAMENT_LEVEL_REQUIREMENT)
            throw new IllegalArgumentException("User level is not enough to enter the tournament");
        if (user.getCoins() < TOURNAMENT_ENTRY_FEE)
            throw new IllegalArgumentException("User does not have enough coins to enter the tournament");

        List<UserTournamentGroup> unclaimedRewards = userTournamentGroupRepository
                .findPreviousUnclaimedTournamentRewards(userId, 3, false, Date.from(Instant.now()));

        if (!unclaimedRewards.isEmpty())
            throw new IllegalArgumentException("User has unclaimed rewards");

        Tournament tournament = getCurrentTournament();

        boolean alreadyEntered = userTournamentGroupRepository
                .findByUserIdAndTournamentId(userId, tournament.getId())
                .isPresent();

        if (alreadyEntered)
            throw new IllegalArgumentException("User has already entered the tournament");

        TournamentGroup tournamentGroup = tournamentGroupRepository
                .findByTournamentAndUserTournamentGroups_User_CountryNot(tournament, user.getCountry())
                .orElse(new TournamentGroup(tournament));

        if (tournamentGroup.getUserTournamentGroups().size() >= TOURNAMENT_GROUP_SIZE) {
            Date now = Date.from(Instant.now());
            tournamentGroup.setStartDate(now);
        }
        tournamentGroup = tournamentGroupRepository.save(tournamentGroup);
        UserTournamentGroup userTournamentGroup = new UserTournamentGroup();
        userTournamentGroup.setUser(user);
        userTournamentGroup.setTournamentGroup(tournamentGroup);
        userTournamentGroup.setRanking(tournamentGroup.getUserTournamentGroups().size() + 1);
        userTournamentGroup.setEnteredAt(Date.from(Instant.now()));
        userTournamentGroupRepository.save(userTournamentGroup);
    }

    public User claimReward(Long id, UUID userId) {
        UserTournamentGroup userTournamentGroup = userTournamentGroupRepository
                .findByUserIdAndTournamentId(userId, id)
                .orElseThrow(() -> new ObjectNotFoundException(userId, UserTournamentGroup.class.getName()));
        Tournament tournament = userTournamentGroup.getTournamentGroup().getTournament();
        User user = userTournamentGroup.getUser();

        if (tournament.getEndDateTime().after(Date.from(Instant.now())))
            throw new IllegalArgumentException("Tournament has not ended yet");
        if (userTournamentGroup.isRewardClaimed())
            throw new IllegalArgumentException("Reward already claimed");
        if (!isEligibleForReward(userTournamentGroup.getRanking()))
            throw new IllegalArgumentException("User is not eligible for a reward");


        if (userTournamentGroup.getRanking() == 1)  // TODO: implement reward calculation
            user.setCoins(user.getCoins() + 10000);
        else if (userTournamentGroup.getRanking() == 2)
            user.setCoins(user.getCoins() + 5000);

        userTournamentGroup.setRewardClaimed(true);
        userTournamentGroupRepository.save(userTournamentGroup);
        userRepository.save(user);
        return user;
    }

    public boolean isInActiveTournament(User user) {
        try {
            Tournament tournament = getCurrentTournament();
            Optional<UserTournamentGroup> userTournamentGroup = userTournamentGroupRepository
                    .findByUserIdAndTournamentId(user.getId(), tournament.getId());
            return userTournamentGroup.isPresent() && userTournamentGroup.get().getTournamentGroup().getStartDate() != null;
        } catch (ObjectNotFoundException e) {
            return false;
        }
    }

    public boolean isEligibleForReward(int rank) {
        return rank <= 3;
    }

    public Tournament getCurrentTournament() {
        Instant now = Instant.now();
        Date dateNow = Date.from(now);

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalTime startTime = LocalTime.of(DAILY_TOURNAMENT_START_HOUR, 0);
        LocalTime finishTime = LocalTime.of(DAILY_TOURNAMENT_END_HOUR, 0);
        Date startDate = Date.from(ZonedDateTime.of(today, startTime, ZoneOffset.UTC).toInstant());
        Date finishDate = Date.from(ZonedDateTime.of(today, finishTime, ZoneOffset.UTC).toInstant());

        Optional<Tournament> tournament = tournamentRepository.findOngoingTournament(dateNow);

        if (tournament.isPresent()) {
            return tournament.get();
        } else if (dateNow.after(startDate) && dateNow.before(finishDate)) {
            return tournamentRepository.save(new Tournament(startDate, finishDate));
        } else {
            throw new ObjectNotFoundException(dateNow, Tournament.class.getName());
        }
    }

    public void updateUserLevel(User user) {
        Tournament tournament = getCurrentTournament();
        UserTournamentGroup userTournamentGroup = userTournamentGroupRepository
                .findByUserIdAndTournamentId(user.getId(), tournament.getId())
                .orElseThrow(() -> new ObjectNotFoundException(user.getId(), UserTournamentGroup.class.getName()));

        if (userTournamentGroup.getRanking() > 1) {
            UserTournamentGroup rival = userTournamentGroupRepository.findByTournamentGroupAndRanking(
                    userTournamentGroup.getTournamentGroup(),
                    userTournamentGroup.getRanking() - 1)
                    .orElseThrow(() -> new ObjectNotFoundException(userTournamentGroup.getRanking() - 1, UserTournamentGroup.class.getName()));

            if (rival.getScore() < userTournamentGroup.getScore()) {
                userTournamentGroup.setRanking(userTournamentGroup.getRanking() - 1);
                rival.setRanking(rival.getRanking() + 1);
                userTournamentGroupRepository.save(rival);
            }
        }

        userTournamentGroup.setScore(userTournamentGroup.getScore() + 1);
        userTournamentGroupRepository.save(userTournamentGroup);
    }
}
