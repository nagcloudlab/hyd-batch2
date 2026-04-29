package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.Todo;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    // You can add custom query methods here if needed

}
