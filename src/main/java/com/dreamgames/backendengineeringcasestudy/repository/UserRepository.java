package com.dreamgames.backendengineeringcasestudy.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dreamgames.backendengineeringcasestudy.model.User;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
}
