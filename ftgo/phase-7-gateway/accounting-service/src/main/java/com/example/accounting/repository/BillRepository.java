package com.example.accounting.repository;

import com.example.accounting.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByOrderId(Long orderId);
}
