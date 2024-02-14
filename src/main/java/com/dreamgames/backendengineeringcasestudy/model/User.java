package com.dreamgames.backendengineeringcasestudy.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
public class User {
    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "country_id", referencedColumnName = "id")
    private Country country;

    private int level = 1;

    private int coins = 5000;

    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    List<UserTournamentGroup> userTournamentGroups;

    public User(Country country) {
        this.country = country;
    }
}
