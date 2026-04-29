package com.example.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // You can add custom query methods here if needed

    Optional<User> findByUsername(String username);

}
