package com.dreamgames.backendengineeringcasestudy.repository;

import com.dreamgames.backendengineeringcasestudy.model.RewardBucket;
import com.dreamgames.backendengineeringcasestudy.model.Tournament;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
public class RewardBucketRepositoryTest {

    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private RewardBucketRepository underTest;

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
        tournamentRepository.deleteAll();
    }

    @Test
    void shouldFindRewardBucketByRank() {
        // given
        Tournament tournament = Tournament
                .builder()
                .id(1L)
                .build();
        RewardBucket rewardBucket = RewardBucket
                .builder()
                .tournament(tournament)
                .startRank(1)
                .endRank(10)
                .rewardAmount(100)
                .build();
        tournamentRepository.save(tournament);
        underTest.save(rewardBucket);

        // when
        Optional<RewardBucket> expected = underTest.findRewardBucketByRank(tournament.getId(), rewardBucket.getStartRank());

        // then
        assertThat(expected).isPresent();
    }

    @Test
    void shouldNotFindRewardBucketByRank() {
        // given
        Tournament tournament = Tournament
                .builder()
                .id(1L)
                .build();
        RewardBucket rewardBucket = RewardBucket
                .builder()
                .tournament(tournament)
                .startRank(1)
                .endRank(10)
                .rewardAmount(100)
                .build();
        tournamentRepository.save(tournament);
        underTest.save(rewardBucket);

        // when
        Optional<RewardBucket> expected = underTest.findRewardBucketByRank(1L, 11);

        // then
        assertThat(expected).isNotPresent();
    }
}
