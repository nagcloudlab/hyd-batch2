package com.example.repository;

import com.example.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantId(Long restaurantId);
    List<MenuItem> findByRestaurantIdAndAvailableTrue(Long restaurantId);
}
