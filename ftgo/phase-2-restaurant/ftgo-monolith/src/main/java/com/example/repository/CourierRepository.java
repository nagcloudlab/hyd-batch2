package com.example.repository;

import com.example.entity.Courier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourierRepository extends JpaRepository<Courier, Long> {
    List<Courier> findByAvailableTrue();
}
