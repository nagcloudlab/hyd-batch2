package com.example.service;

import com.example.dto.MenuItemDTO;
import com.example.dto.RestaurantDTO;
import com.example.entity.*;
import com.example.event.*;
import com.example.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    private final FoodOrderRepository foodOrderRepository;
    private final ConsumerRepository consumerRepository;
    private final RestaurantServiceClient restaurantServiceClient;
    private final NotificationEventProducer notificationEventProducer;
    private final KitchenEventProducer kitchenEventProducer;
    private final AccountingEventProducer accountingEventProducer;

    public FoodOrder placeOrder(Long consumerId, Long restaurantId, List<Long> menuItemIds, List<Integer> quantities,
            String deliveryAddress) {

        Consumer consumer = consumerRepository.findById(consumerId)
                .orElseThrow(() -> new RuntimeException("Consumer not found"));

        RestaurantDTO restaurant = restaurantServiceClient.getRestaurant(restaurantId);

        FoodOrder order = FoodOrder.builder()
                .consumer(consumer)
                .restaurantId(restaurant.getId())
                .restaurantName(restaurant.getName())
                .status(OrderStatus.PLACED)
                .orderTime(LocalDateTime.now())
                .deliveryAddress(deliveryAddress)
                .paymentStatus("PENDING")
                .lineItems(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        List<String> itemDescriptions = new ArrayList<>();

        for (int i = 0; i < menuItemIds.size(); i++) {
            MenuItemDTO menuItem = restaurantServiceClient.getMenuItem(menuItemIds.get(i));
            int qty = quantities.get(i);
            BigDecimal lineTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(qty));
            total = total.add(lineTotal);

            OrderLineItem lineItem = OrderLineItem.builder()
                    .foodOrder(order)
                    .menuItemId(menuItem.getId())
                    .menuItemName(menuItem.getName())
                    .quantity(qty)
                    .price(lineTotal)
                    .build();
            order.getLineItems().add(lineItem);
            itemDescriptions.add(menuItem.getName() + " x" + qty);
        }
        order.setTotalAmount(total);

        FoodOrder savedOrder = foodOrderRepository.save(order);

        // Publish PAYMENT_REQUESTED to accounting-service (async)
        accountingEventProducer.publish(AccountingEvent.builder()
                .eventType("PAYMENT_REQUESTED")
                .orderId(savedOrder.getId())
                .amount(total)
                .restaurantName(restaurant.getName())
                .build());

        // Notify consumer
        notificationEventProducer.publish(NotificationEvent.builder()
                .eventType("ORDER_CONFIRMED")
                .orderId(savedOrder.getId())
                .consumerPhone(consumer.getPhone())
                .consumerEmail(consumer.getEmail())
                .restaurantPhone(restaurant.getPhone())
                .restaurantName(restaurant.getName())
                .totalAmount(savedOrder.getTotalAmount())
                .build());

        // Publish to kitchen-service
        kitchenEventProducer.publish(KitchenEvent.builder()
                .eventType("ORDER_PLACED")
                .orderId(savedOrder.getId())
                .restaurantId(restaurant.getId())
                .restaurantName(restaurant.getName())
                .consumerName(consumer.getName())
                .consumerPhone(consumer.getPhone())
                .deliveryAddress(deliveryAddress)
                .totalAmount(total)
                .items(String.join(", ", itemDescriptions))
                .build());

        return savedOrder;
    }

    public List<FoodOrder> getOrdersByConsumer(Long consumerId) {
        return foodOrderRepository.findByConsumerId(consumerId);
    }

    public List<FoodOrder> getOrdersByRestaurant(Long restaurantId) {
        return foodOrderRepository.findByRestaurantId(restaurantId);
    }

    public FoodOrder getOrder(Long orderId) {
        return foodOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }
}
