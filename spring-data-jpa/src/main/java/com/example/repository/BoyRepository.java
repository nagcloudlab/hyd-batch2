package com.example.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.entity.Boy;

public interface BoyRepository extends JpaRepository<Boy, Integer> {

    // spring data - DSL (Domain Specific Language)
    // Optional<Boy> findByName(String name);

    // or

    @Query("SELECT b FROM Boy b WHERE b.name = :name") // JPQL (Java Persistence Query Language)
    Optional<Boy> foo(String name);

}
