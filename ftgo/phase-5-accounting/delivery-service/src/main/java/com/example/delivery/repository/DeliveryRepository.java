package com.example.delivery.repository;

import com.example.delivery.entity.Delivery;
import com.example.delivery.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByCourierId(Long courierId);
    List<Delivery> findByStatus(DeliveryStatus status);
    Optional<Delivery> findByOrderId(Long orderId);
}
