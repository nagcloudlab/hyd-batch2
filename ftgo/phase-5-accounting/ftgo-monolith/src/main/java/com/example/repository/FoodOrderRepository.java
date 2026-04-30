package com.example.repository;

import com.example.entity.FoodOrder;
import com.example.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {
    List<FoodOrder> findByConsumerId(Long consumerId);
    List<FoodOrder> findByRestaurantId(Long restaurantId);
    List<FoodOrder> findByStatus(OrderStatus status);
}
