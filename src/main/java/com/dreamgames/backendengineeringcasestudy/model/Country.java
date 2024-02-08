package com.dreamgames.backendengineeringcasestudy.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    private String code;

    private String name;

    @OneToMany(mappedBy = "country")
    private List<User> users;
}