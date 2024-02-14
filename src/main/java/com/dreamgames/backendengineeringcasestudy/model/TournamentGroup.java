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
public class TournamentGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id", referencedColumnName = "id")
    private Tournament tournament;

    @OneToMany(mappedBy = "tournamentGroup")
    private List<UserTournamentGroup> userTournamentGroups;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;

    public TournamentGroup(Tournament tournament) {
        this.tournament = tournament;
        userTournamentGroups = List.of();
    }
}
