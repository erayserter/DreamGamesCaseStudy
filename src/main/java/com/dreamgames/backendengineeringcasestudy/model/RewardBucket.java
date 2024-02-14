package com.dreamgames.backendengineeringcasestudy.model;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardBucket {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private int rewardAmount;

    private int startRank;

    private int endRank;

    @ManyToOne
    private Tournament tournament;
}
