package com.dreamgames.backendengineeringcasestudy.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    private int score = 0;

    private boolean hasReward = false;

    private boolean isRewardClaimed = false;

    @Temporal(TemporalType.TIMESTAMP)
    private Date enteredAt = Date.from(Instant.now());

    public UserTournamentGroup(User user,
                               TournamentGroup tournamentGroup) {
        this.user = user;
        this.tournamentGroup = tournamentGroup;
    }
}
