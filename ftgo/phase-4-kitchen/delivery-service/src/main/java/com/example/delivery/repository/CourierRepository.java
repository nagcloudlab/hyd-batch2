package com.example.delivery.repository;

import com.example.delivery.entity.Courier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourierRepository extends JpaRepository<Courier, Long> {
    List<Courier> findByAvailableTrue();
}
