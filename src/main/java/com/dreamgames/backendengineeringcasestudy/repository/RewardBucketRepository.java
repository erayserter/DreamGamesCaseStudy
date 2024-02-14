package com.dreamgames.backendengineeringcasestudy.repository;

import com.dreamgames.backendengineeringcasestudy.model.Country;
import com.dreamgames.backendengineeringcasestudy.model.RewardBucket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RewardBucketRepository extends JpaRepository<Country, Integer> {

    @Query("SELECT rb " +
            "FROM RewardBucket as rb " +
            "WHERE rb.startRank <= :rank " +
            "AND rb.endRank >= :rank")
    Optional<RewardBucket> findRewardBucketByRank(@Param("rank") int rank);
}
