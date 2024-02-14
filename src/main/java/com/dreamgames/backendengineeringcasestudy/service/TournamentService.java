package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.exception.BadRequestException;
import com.dreamgames.backendengineeringcasestudy.exception.EntityNotFoundException;
import com.dreamgames.backendengineeringcasestudy.model.Tournament;
import com.dreamgames.backendengineeringcasestudy.model.TournamentGroup;
import com.dreamgames.backendengineeringcasestudy.model.User;
import com.dreamgames.backendengineeringcasestudy.model.UserTournamentGroup;
import com.dreamgames.backendengineeringcasestudy.repository.TournamentGroupRepository;
import com.dreamgames.backendengineeringcasestudy.repository.TournamentRepository;
import com.dreamgames.backendengineeringcasestudy.repository.UserRepository;
import com.dreamgames.backendengineeringcasestudy.repository.UserTournamentGroupRepository;
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
    private final LeaderboardService leaderboardService;

    public TournamentService(TournamentRepository tournamentRepository,
                             TournamentGroupRepository tournamentGroupRepository,
                             UserTournamentGroupRepository userTournamentGroupRepository,
                             UserRepository userRepository,
                             LeaderboardService leaderboardService) {
        this.tournamentRepository = tournamentRepository;
        this.tournamentGroupRepository = tournamentGroupRepository;
        this.userTournamentGroupRepository = userTournamentGroupRepository;
        this.userRepository = userRepository;
        this.leaderboardService = leaderboardService;
    }

    public void enterTournament(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (user.getLevel() < TOURNAMENT_LEVEL_REQUIREMENT)
            throw new BadRequestException("User level is not enough to enter the tournament");
        if (user.getCoins() < TOURNAMENT_ENTRY_FEE)
            throw new BadRequestException("User does not have enough coins to enter the tournament");

        List<UserTournamentGroup> unclaimedRewards = userTournamentGroupRepository
                .findPreviousUnclaimedTournamentRewards(userId, false, Date.from(Instant.now()));
        unclaimedRewards = unclaimedRewards
                .stream()
                .filter(userTournamentGroup ->
                        leaderboardService.getGroupRank(
                                userId,
                                userTournamentGroup.getTournamentGroup().getTournament().getId()
                        ) <= 3)
                .toList();

        if (!unclaimedRewards.isEmpty())
            throw new BadRequestException("User has unclaimed rewards");

        Tournament tournament = getCurrentTournament();

        boolean alreadyEntered = userTournamentGroupRepository
                .findByUserIdAndTournamentId(userId, tournament.getId())
                .isPresent();

        if (alreadyEntered)
            throw new BadRequestException("User has already entered the tournament");

        TournamentGroup tournamentGroup = tournamentGroupRepository
                .findHasNoUsersWithCountry(tournament, user.getCountry())
                .orElse(new TournamentGroup(tournament));

        if (tournamentGroup.getUserTournamentGroups().size() >= TOURNAMENT_GROUP_SIZE - 1) {
            Date now = Date.from(Instant.now());
            tournamentGroup.setStartDate(now);
        }
        tournamentGroup = tournamentGroupRepository.save(tournamentGroup);
        UserTournamentGroup userTournamentGroup = new UserTournamentGroup(user, tournamentGroup);
        userTournamentGroup.setEnteredAt(Date.from(Instant.now()));
        userTournamentGroupRepository.save(userTournamentGroup);
    }

    public User claimReward(Long id, UUID userId) {
        UserTournamentGroup userTournamentGroup = userTournamentGroupRepository
                .findByUserIdAndTournamentId(userId, id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        Tournament tournament = userTournamentGroup.getTournamentGroup().getTournament();
        User user = userTournamentGroup.getUser();

        if (tournament.getEndDateTime().after(Date.from(Instant.now())))
            throw new BadRequestException("Tournament has not ended yet");
        if (userTournamentGroup.isRewardClaimed())
            throw new BadRequestException("Reward already claimed");

        user.setCoins(user.getCoins() + calculateReward(userTournamentGroup));

        userTournamentGroup.setRewardClaimed(true);
        userTournamentGroupRepository.save(userTournamentGroup);
        userRepository.save(user);
        return user;
    }

    public int calculateReward(UserTournamentGroup userTournamentGroup) {  // TODO: implement reward calculation
        List<UserTournamentGroup> scores = userTournamentGroupRepository
                .orderGroupByScores(userTournamentGroup.getTournamentGroup().getId());
        int rank = scores.indexOf(userTournamentGroup) + 1;

        if (rank == 1)
            return 10000;
        else if (rank == 2)
            return 5000;

        throw new BadRequestException("User is not eligible for a reward");
    }

    public boolean isInActiveTournament(User user) {
        try {
            Tournament tournament = getCurrentTournament();
            Optional<UserTournamentGroup> userTournamentGroup = userTournamentGroupRepository
                    .findByUserIdAndTournamentId(user.getId(), tournament.getId());
            return userTournamentGroup.isPresent() && userTournamentGroup.get().getTournamentGroup().getStartDate() != null;
        } catch (BadRequestException e) {
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
            return tournamentRepository.save(new Tournament(startDate, finishDate, TOURNAMENT_GROUP_SIZE));
        } else {
            throw new BadRequestException("No active tournament");
        }
    }

    public void updateUserLevel(User user) {
        Tournament tournament = getCurrentTournament();
        UserTournamentGroup userTournamentGroup = userTournamentGroupRepository
                .findByUserIdAndTournamentId(user.getId(), tournament.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found in the tournament"));
        userTournamentGroup.setScore(userTournamentGroup.getScore() + 1);
        userTournamentGroupRepository.save(userTournamentGroup);
    }
}
