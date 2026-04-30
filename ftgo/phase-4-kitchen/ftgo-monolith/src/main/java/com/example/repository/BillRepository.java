package com.example.repository;

import com.example.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByFoodOrderId(Long orderId);
}
