package com.example.restaurant.web;

import com.example.restaurant.entity.MenuItem;
import com.example.restaurant.entity.Restaurant;
import com.example.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class RestaurantApiController {

    private final RestaurantService restaurantService;

    @GetMapping("/restaurants")
    public List<Restaurant> getAllRestaurants() {
        log.info("[REST-API] GET /api/restaurants");
        return restaurantService.getAllRestaurants();
    }

    @GetMapping("/restaurants/active")
    public List<Restaurant> getActiveRestaurants() {
        log.info("[REST-API] GET /api/restaurants/active");
        return restaurantService.getActiveRestaurants();
    }

    @GetMapping("/restaurants/{id}")
    public Restaurant getRestaurant(@PathVariable Long id) {
        log.info("[REST-API] GET /api/restaurants/{}", id);
        return restaurantService.getRestaurant(id);
    }

    @GetMapping("/restaurants/{id}/menu-items")
    public List<MenuItem> getMenuItems(@PathVariable Long id) {
        log.info("[REST-API] GET /api/restaurants/{}/menu-items", id);
        return restaurantService.getMenuItems(id);
    }

    @GetMapping("/menu-items/{id}")
    public MenuItem getMenuItem(@PathVariable Long id) {
        log.info("[REST-API] GET /api/menu-items/{}", id);
        return restaurantService.getMenuItem(id);
    }

    @PostMapping("/restaurants/{id}/menu-items")
    public MenuItem addMenuItem(@PathVariable Long id, @RequestBody MenuItem menuItem) {
        log.info("[REST-API] POST /api/restaurants/{}/menu-items - {}", id, menuItem.getName());
        return restaurantService.addMenuItem(id, menuItem);
    }

    @PostMapping("/menu-items/{id}/toggle")
    public MenuItem toggleAvailability(@PathVariable Long id) {
        log.info("[REST-API] POST /api/menu-items/{}/toggle", id);
        return restaurantService.toggleMenuItemAvailability(id);
    }
}
