package com.dreamgames.backendengineeringcasestudy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
public class UserTournamentGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "tournament_group_id", referencedColumnName = "id")
    private TournamentGroup tournamentGroup;

    @Column(columnDefinition = "integer default 0")
    private int score;

    @Column(columnDefinition = "boolean default false")
    private boolean isRewardClaimed;

    @Temporal(TemporalType.TIMESTAMP)
    private Date enteredAt;
}
