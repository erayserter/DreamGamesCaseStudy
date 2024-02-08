package com.dreamgames.backendengineeringcasestudy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "country_id", referencedColumnName = "id")
    private Country country;

    @Column(columnDefinition = "integer default 1")
    private int level;

    @Column(columnDefinition = "integer default 5000")
    private int coins;

    @OneToMany(mappedBy = "user")
    List<UserTournamentGroup> userTournamentGroups;

    public User(Country country) {
        this.country = country;
    }
}
