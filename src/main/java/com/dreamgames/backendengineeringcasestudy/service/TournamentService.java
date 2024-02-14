package com.dreamgames.backendengineeringcasestudy.service;

import com.dreamgames.backendengineeringcasestudy.exception.BadRequestException;
import com.dreamgames.backendengineeringcasestudy.exception.EntityNotFoundException;
import com.dreamgames.backendengineeringcasestudy.model.*;
import com.dreamgames.backendengineeringcasestudy.repository.*;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TournamentService {
    public final static int TOURNAMENT_LEVEL_REQUIREMENT = 20;
    public final static int TOURNAMENT_ENTRY_FEE = 1000;
    public final static int DAILY_TOURNAMENT_START_HOUR = 0;
    public final static int DAILY_TOURNAMENT_END_HOUR = 20;

    private final TournamentRepository tournamentRepository;
    private final TournamentGroupRepository tournamentGroupRepository;
    private final UserTournamentGroupRepository userTournamentGroupRepository;
    private final RewardBucketRepository rewardBucketRepository;
    private final UserRepository userRepository;

    public TournamentService(TournamentRepository tournamentRepository,
                             TournamentGroupRepository tournamentGroupRepository,
                             UserTournamentGroupRepository userTournamentGroupRepository,
                             RewardBucketRepository rewardBucketRepository,
                             UserRepository userRepository) {
        this.tournamentRepository = tournamentRepository;
        this.tournamentGroupRepository = tournamentGroupRepository;
        this.userTournamentGroupRepository = userTournamentGroupRepository;
        this.rewardBucketRepository = rewardBucketRepository;
        this.userRepository = userRepository;
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

        if (tournamentGroup.getUserTournamentGroups().size() >= tournament.getGroupSizes() - 1) {
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

    public int calculateReward(UserTournamentGroup userTournamentGroup) {
        List<UserTournamentGroup> scores = userTournamentGroupRepository
                .orderGroupByScores(userTournamentGroup.getTournamentGroup().getId());
        int rank = scores.indexOf(userTournamentGroup) + 1;

        RewardBucket rewardBucket = rewardBucketRepository
                .findRewardBucketByRank(rank)
                .orElseThrow(() -> new BadRequestException("User is not eligible for a reward"));

        return rewardBucket.getRewardAmount();
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

        return tournamentRepository.findOngoingTournament(dateNow)
                .orElseThrow(() -> new BadRequestException("No active tournament"));
    }

    public void updateUserLevel(User user) {
        Tournament tournament = getCurrentTournament();
        UserTournamentGroup userTournamentGroup = userTournamentGroupRepository
                .findByUserIdAndTournamentId(user.getId(), tournament.getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found in the tournament"));
        userTournamentGroup.setScore(userTournamentGroup.getScore() + 1);
        userTournamentGroupRepository.save(userTournamentGroup);
    }

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 * * * *")
    public void createDailyTournament() {
        Optional<Tournament> already = tournamentRepository.findOngoingTournament(Date.from(Instant.now()));

        if (already.isPresent())
            return;

        int levelRequirement = 20;
        int entryFee = 1000;
        int groupSizes = 5;

        int firstPlaceReward = 10000;
        int secondPlaceReward = 5000;

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalTime startTime = LocalTime.of(DAILY_TOURNAMENT_START_HOUR, 0);
        LocalTime finishTime = LocalTime.of(DAILY_TOURNAMENT_END_HOUR, 0);
        Date startDate = Date.from(ZonedDateTime.of(today, startTime, ZoneOffset.UTC).toInstant());
        Date finishDate = Date.from(ZonedDateTime.of(today, finishTime, ZoneOffset.UTC).toInstant());

        Tournament tournament = Tournament
                .builder()
                .startDateTime(startDate)
                .endDateTime(finishDate)
                .groupSizes(groupSizes)
                .entryFee(entryFee)
                .levelRequirement(levelRequirement)
                .build();

        RewardBucket.builder().tournament(tournament).startRank(1).endRank(1).rewardAmount(firstPlaceReward).build();
        RewardBucket.builder().tournament(tournament).startRank(2).endRank(2).rewardAmount(secondPlaceReward).build();

        tournamentRepository.save(tournament);
    }
}
