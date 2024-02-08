package com.dreamgames.backendengineeringcasestudy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class TournamentGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "tournament_id", referencedColumnName = "id")
    private Tournament tournament;

    @OneToMany(mappedBy = "tournamentGroup")
    private List<UserTournamentGroup> userTournamentGroups;

}
