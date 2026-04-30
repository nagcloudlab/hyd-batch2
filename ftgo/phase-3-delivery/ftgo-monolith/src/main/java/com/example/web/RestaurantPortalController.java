package com.example.web;

import com.example.entity.OrderStatus;
import com.example.event.OrderEvent;
import com.example.event.OrderEventProducer;
import com.example.service.OrderService;
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
    private final OrderService orderService;
    private final OrderEventProducer orderEventProducer;

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

    @GetMapping("/{id}/orders")
    public String viewOrders(@PathVariable Long id, Model model) {
        model.addAttribute("restaurant", restaurantServiceClient.getRestaurant(id));
        model.addAttribute("orders", orderService.getOrdersByRestaurant(id));
        model.addAttribute("allRestaurants", restaurantServiceClient.getAllRestaurants());
        return "restaurant/orders";
    }

    @PostMapping("/orders/{orderId}/status")
    public String updateOrderStatus(@PathVariable Long orderId,
                                    @RequestParam String status,
                                    @RequestParam Long restaurantId) {
        OrderStatus newStatus = OrderStatus.valueOf(status);
        var order = orderService.updateOrderStatus(orderId, newStatus);

        // When restaurant marks READY_FOR_PICKUP, publish Kafka event
        // delivery-service consumes this and creates the delivery
        if (newStatus == OrderStatus.READY_FOR_PICKUP) {
            orderEventProducer.publish(OrderEvent.builder()
                    .eventType("ORDER_READY_FOR_PICKUP")
                    .orderId(order.getId())
                    .consumerName(order.getConsumer().getName())
                    .consumerPhone(order.getConsumer().getPhone())
                    .restaurantName(order.getRestaurantName())
                    .deliveryAddress(order.getDeliveryAddress())
                    .totalAmount(order.getTotalAmount())
                    .build());
        }

        return "redirect:/restaurant/" + restaurantId + "/orders";
    }
}
