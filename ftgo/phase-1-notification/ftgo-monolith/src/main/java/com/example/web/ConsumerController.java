package com.example.web;

import com.example.entity.FoodOrder;
import com.example.entity.Restaurant;
import com.example.repository.ConsumerRepository;
import com.example.service.OrderService;
import com.example.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/consumer")
@RequiredArgsConstructor
public class ConsumerController {

    private final RestaurantService restaurantService;
    private final OrderService orderService;
    private final ConsumerRepository consumerRepository;

    // Page 1: Browse restaurants and place orders
    @GetMapping("/restaurants")
    public String browseRestaurants(@RequestParam(defaultValue = "1") Long consumerId, Model model) {
        model.addAttribute("restaurants", restaurantService.getActiveRestaurants());
        model.addAttribute("consumers", consumerRepository.findAll());
        model.addAttribute("consumerId", consumerId);
        return "consumer/restaurants";
    }

    @PostMapping("/orders")
    public String placeOrder(@RequestParam Long consumerId,
                             @RequestParam Long restaurantId,
                             @RequestParam List<Long> menuItemIds,
                             @RequestParam List<Integer> quantities,
                             @RequestParam String deliveryAddress) {
        orderService.placeOrder(consumerId, restaurantId, menuItemIds, quantities, deliveryAddress);
        return "redirect:/consumer/orders?consumerId=" + consumerId;
    }

    // Page 2: My orders
    @GetMapping("/orders")
    public String myOrders(@RequestParam(defaultValue = "1") Long consumerId, Model model) {
        List<FoodOrder> orders = orderService.getOrdersByConsumer(consumerId);
        model.addAttribute("orders", orders);
        model.addAttribute("consumers", consumerRepository.findAll());
        model.addAttribute("consumerId", consumerId);
        return "consumer/orders";
    }
}
