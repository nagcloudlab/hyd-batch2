package com.example.web;

import com.example.entity.MenuItem;
import com.example.entity.OrderStatus;
import com.example.service.DeliveryService;
import com.example.service.OrderService;
import com.example.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/restaurant")
@RequiredArgsConstructor
public class RestaurantPortalController {

    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final DeliveryService deliveryService;

    // Restaurant selection page
    @GetMapping("")
    public String selectRestaurant(Model model) {
        model.addAttribute("restaurants", restaurantService.getAllRestaurants());
        return "restaurant/select";
    }

    // Page 1: Manage menu
    @GetMapping("/{id}/menu")
    public String manageMenu(@PathVariable Long id, Model model) {
        model.addAttribute("restaurant", restaurantService.getRestaurant(id));
        model.addAttribute("menuItems", restaurantService.getMenuItems(id));
        model.addAttribute("allRestaurants", restaurantService.getAllRestaurants());
        return "restaurant/menu";
    }

    @PostMapping("/{id}/menu")
    public String addMenuItem(@PathVariable Long id,
                              @RequestParam String name,
                              @RequestParam BigDecimal price) {
        MenuItem item = MenuItem.builder().name(name).price(price).available(true).build();
        restaurantService.addMenuItem(id, item);
        return "redirect:/restaurant/" + id + "/menu";
    }

    @PostMapping("/menu/{itemId}/toggle")
    public String toggleAvailability(@PathVariable Long itemId,
                                     @RequestParam Long restaurantId) {
        restaurantService.toggleMenuItemAvailability(itemId);
        return "redirect:/restaurant/" + restaurantId + "/menu";
    }

    // Page 2: View incoming orders
    @GetMapping("/{id}/orders")
    public String viewOrders(@PathVariable Long id, Model model) {
        model.addAttribute("restaurant", restaurantService.getRestaurant(id));
        model.addAttribute("orders", orderService.getOrdersByRestaurant(id));
        model.addAttribute("allRestaurants", restaurantService.getAllRestaurants());
        return "restaurant/orders";
    }

    @PostMapping("/orders/{orderId}/status")
    public String updateOrderStatus(@PathVariable Long orderId,
                                    @RequestParam String status,
                                    @RequestParam Long restaurantId) {
        OrderStatus newStatus = OrderStatus.valueOf(status);
        var order = orderService.updateOrderStatus(orderId, newStatus);

        // When restaurant marks READY_FOR_PICKUP, create a delivery entry
        if (newStatus == OrderStatus.READY_FOR_PICKUP) {
            deliveryService.createDelivery(order);
        }

        return "redirect:/restaurant/" + restaurantId + "/orders";
    }
}
