package com.example.web;

import com.example.entity.FoodOrder;
import com.example.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrderApiController {

    private final OrderService orderService;

    @PostMapping
    public Map<String, Object> placeOrder(@RequestBody Map<String, Object> req) {
        @SuppressWarnings("unchecked")
        List<Integer> menuItemIds = ((List<Integer>) req.get("menuItemIds"));
        @SuppressWarnings("unchecked")
        List<Integer> quantities = ((List<Integer>) req.get("quantities"));

        FoodOrder order = orderService.placeOrder(
                ((Number) req.get("consumerId")).longValue(),
                ((Number) req.get("restaurantId")).longValue(),
                menuItemIds.stream().map(Integer::longValue).toList(),
                quantities,
                (String) req.get("deliveryAddress")
        );

        return Map.of(
                "orderId", order.getId(),
                "status", order.getStatus().name(),
                "restaurantName", order.getRestaurantName(),
                "totalAmount", order.getTotalAmount(),
                "deliveryAddress", order.getDeliveryAddress()
        );
    }

    @GetMapping("/consumer/{consumerId}")
    public List<Map<String, Object>> getConsumerOrders(@PathVariable Long consumerId) {
        return orderService.getOrdersByConsumer(consumerId).stream().map(o -> Map.<String, Object>of(
                "orderId", o.getId(),
                "status", o.getStatus().name(),
                "restaurantName", o.getRestaurantName(),
                "totalAmount", o.getTotalAmount(),
                "deliveryAddress", o.getDeliveryAddress(),
                "orderTime", o.getOrderTime().toString(),
                "paymentStatus", o.getPaymentStatus() != null ? o.getPaymentStatus() : "PENDING",
                "deliveryStatus", o.getDeliveryStatus() != null ? o.getDeliveryStatus() : ""
        )).toList();
    }
}
