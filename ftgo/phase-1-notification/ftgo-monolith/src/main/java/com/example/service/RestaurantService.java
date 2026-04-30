package com.example.service;

import com.example.entity.MenuItem;
import com.example.entity.Restaurant;
import com.example.repository.MenuItemRepository;
import com.example.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    public List<Restaurant> getActiveRestaurants() {
        return restaurantRepository.findByActiveTrue();
    }

    public Restaurant getRestaurant(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Restaurant not found: " + id));
    }

    public List<MenuItem> getMenuItems(Long restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId);
    }

    public List<MenuItem> getAvailableMenuItems(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId);
    }

    public MenuItem addMenuItem(Long restaurantId, MenuItem menuItem) {
        Restaurant restaurant = getRestaurant(restaurantId);
        menuItem.setRestaurant(restaurant);
        return menuItemRepository.save(menuItem);
    }

    public MenuItem toggleMenuItemAvailability(Long menuItemId) {
        MenuItem item = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new RuntimeException("Menu item not found: " + menuItemId));
        item.setAvailable(!item.isAvailable());
        return menuItemRepository.save(item);
    }
}
