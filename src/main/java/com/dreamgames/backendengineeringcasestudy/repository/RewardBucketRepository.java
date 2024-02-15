package com.dreamgames.backendengineeringcasestudy.repository;

import com.dreamgames.backendengineeringcasestudy.model.Country;
import com.dreamgames.backendengineeringcasestudy.model.RewardBucket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RewardBucketRepository extends JpaRepository<RewardBucket, Integer> {

    @Query("SELECT rb " +
            "FROM RewardBucket as rb " +
            "WHERE rb.tournament.id = :tournamentId " +
            "AND rb.startRank <= :rank " +
            "AND rb.endRank >= :rank")
    Optional<RewardBucket> findRewardBucketByRank(@Param("tournamentId") Long tournamentId, @Param("rank") int rank);
}
