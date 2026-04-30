package com.example.web;

import com.example.service.RestaurantServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/restaurant")
@RequiredArgsConstructor
public class RestaurantPortalController {

    private final RestaurantServiceClient restaurantServiceClient;

    @GetMapping("")
    public String selectRestaurant(Model model) {
        model.addAttribute("restaurants", restaurantServiceClient.getAllRestaurants());
        return "restaurant/select";
    }

    @GetMapping("/{id}/menu")
    public String manageMenu(@PathVariable Long id, Model model) {
        model.addAttribute("restaurant", restaurantServiceClient.getRestaurant(id));
        model.addAttribute("menuItems", restaurantServiceClient.getMenuItems(id));
        model.addAttribute("allRestaurants", restaurantServiceClient.getAllRestaurants());
        return "restaurant/menu";
    }

    @PostMapping("/{id}/menu")
    public String addMenuItem(@PathVariable Long id,
                              @RequestParam String name,
                              @RequestParam BigDecimal price) {
        restaurantServiceClient.addMenuItem(id, name, price);
        return "redirect:/restaurant/" + id + "/menu";
    }

    @PostMapping("/menu/{itemId}/toggle")
    public String toggleAvailability(@PathVariable Long itemId,
                                     @RequestParam Long restaurantId) {
        restaurantServiceClient.toggleMenuItemAvailability(itemId);
        return "redirect:/restaurant/" + restaurantId + "/menu";
    }

    // Order management moved to kitchen-service (:8084)
}
