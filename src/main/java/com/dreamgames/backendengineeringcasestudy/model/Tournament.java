package com.dreamgames.backendengineeringcasestudy.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endDateTime;

    private int groupSizes;

    private int levelRequirement;

    private int entryFee;

    @OneToMany(mappedBy = "tournament")
    private List<RewardBucket> rewardBuckets;

    @OneToMany(mappedBy = "tournament")
    private List<TournamentGroup> groups;
}
