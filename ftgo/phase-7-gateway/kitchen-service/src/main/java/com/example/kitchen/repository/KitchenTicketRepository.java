package com.example.kitchen.repository;

import com.example.kitchen.entity.KitchenTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface KitchenTicketRepository extends JpaRepository<KitchenTicket, Long> {
    List<KitchenTicket> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);
    Optional<KitchenTicket> findByOrderId(Long orderId);
}
