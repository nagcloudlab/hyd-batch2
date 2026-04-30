package com.example.service;

import com.example.dto.MenuItemDTO;
import com.example.dto.RestaurantDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class RestaurantServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public RestaurantServiceClient(RestTemplate restTemplate,
            @Value("${restaurant-service.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @CircuitBreaker(name = "restaurant-service", fallbackMethod = "getAllRestaurantsFallback")
    @Retry(name = "restaurant-service")
    @RateLimiter(name = "restaurant-service")
    public List<RestaurantDTO> getAllRestaurants() {
        log.info("[REST-CLIENT] GET {}/api/restaurants", baseUrl);
        return restTemplate.exchange(baseUrl + "/api/restaurants",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<RestaurantDTO>>() {}).getBody();
    }

    @CircuitBreaker(name = "restaurant-service", fallbackMethod = "getActiveRestaurantsFallback")
    @Retry(name = "restaurant-service")
    @RateLimiter(name = "restaurant-service")
    public List<RestaurantDTO> getActiveRestaurants() {
        log.info("[REST-CLIENT] GET {}/api/restaurants/active", baseUrl);
        return restTemplate.exchange(baseUrl + "/api/restaurants/active",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<RestaurantDTO>>() {}).getBody();
    }

    @CircuitBreaker(name = "restaurant-service", fallbackMethod = "getRestaurantFallback")
    @Retry(name = "restaurant-service")
    @RateLimiter(name = "restaurant-service")
    public RestaurantDTO getRestaurant(Long id) {
        log.info("[REST-CLIENT] GET {}/api/restaurants/{}", baseUrl, id);
        return restTemplate.getForObject(baseUrl + "/api/restaurants/" + id, RestaurantDTO.class);
    }

    @CircuitBreaker(name = "restaurant-service", fallbackMethod = "getMenuItemsFallback")
    @Retry(name = "restaurant-service")
    @RateLimiter(name = "restaurant-service")
    public List<MenuItemDTO> getMenuItems(Long restaurantId) {
        log.info("[REST-CLIENT] GET {}/api/restaurants/{}/menu-items", baseUrl, restaurantId);
        return restTemplate.exchange(baseUrl + "/api/restaurants/" + restaurantId + "/menu-items",
                HttpMethod.GET, null,
                new ParameterizedTypeReference<List<MenuItemDTO>>() {}).getBody();
    }

    @CircuitBreaker(name = "restaurant-service", fallbackMethod = "getMenuItemFallback")
    @Retry(name = "restaurant-service")
    public MenuItemDTO getMenuItem(Long menuItemId) {
        log.info("[REST-CLIENT] GET {}/api/menu-items/{}", baseUrl, menuItemId);
        return restTemplate.getForObject(baseUrl + "/api/menu-items/" + menuItemId, MenuItemDTO.class);
    }

    @CircuitBreaker(name = "restaurant-service")
    @Retry(name = "restaurant-service")
    public MenuItemDTO addMenuItem(Long restaurantId, String name, BigDecimal price) {
        log.info("[REST-CLIENT] POST {}/api/restaurants/{}/menu-items", baseUrl, restaurantId);
        MenuItemDTO dto = MenuItemDTO.builder().name(name).price(price).available(true).build();
        return restTemplate.postForObject(baseUrl + "/api/restaurants/" + restaurantId + "/menu-items",
                dto, MenuItemDTO.class);
    }

    @CircuitBreaker(name = "restaurant-service")
    @Retry(name = "restaurant-service")
    public void toggleMenuItemAvailability(Long menuItemId) {
        log.info("[REST-CLIENT] POST {}/api/menu-items/{}/toggle", baseUrl, menuItemId);
        restTemplate.postForObject(baseUrl + "/api/menu-items/" + menuItemId + "/toggle",
                null, MenuItemDTO.class);
    }

    // ── Fallback Methods (invoked when circuit is OPEN or all retries exhausted) ──

    public List<RestaurantDTO> getAllRestaurantsFallback(Throwable t) {
        log.warn("[CIRCUIT-BREAKER] restaurant-service is DOWN — returning empty list. Cause: {}", t.getMessage());
        return Collections.emptyList();
    }

    public List<RestaurantDTO> getActiveRestaurantsFallback(Throwable t) {
        log.warn("[CIRCUIT-BREAKER] restaurant-service is DOWN — returning empty list. Cause: {}", t.getMessage());
        return Collections.emptyList();
    }

    public RestaurantDTO getRestaurantFallback(Long id, Throwable t) {
        log.warn("[CIRCUIT-BREAKER] restaurant-service is DOWN — returning placeholder for id {}. Cause: {}", id, t.getMessage());
        return RestaurantDTO.builder()
                .id(id)
                .name("Restaurant (unavailable)")
                .address("Service temporarily unavailable")
                .phone("")
                .active(true)
                .menuItems(Collections.emptyList())
                .build();
    }

    public List<MenuItemDTO> getMenuItemsFallback(Long restaurantId, Throwable t) {
        log.warn("[CIRCUIT-BREAKER] restaurant-service is DOWN — returning empty menu. Cause: {}", t.getMessage());
        return Collections.emptyList();
    }

    public MenuItemDTO getMenuItemFallback(Long menuItemId, Throwable t) {
        log.error("[CIRCUIT-BREAKER] restaurant-service is DOWN — cannot fetch menu item {}. Cause: {}", menuItemId, t.getMessage());
        throw new RuntimeException("Restaurant service is unavailable — cannot place order. Try again later.");
    }
}
