package com.dreamgames.backendengineeringcasestudy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;


@Entity
@Getter
@Setter
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startDateTime;

    @Temporal(TemporalType.TIMESTAMP)
    private Date endDateTime;

    @OneToMany(mappedBy = "tournament")
    private List<TournamentGroup> groups;
}
