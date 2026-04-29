package com.example.service;

import com.example.entity.*;
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
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final PaymentService paymentService;
    private final BillingService billingService;
    private final NotificationService notificationService;

    public FoodOrder placeOrder(Long consumerId, Long restaurantId, List<Long> menuItemIds, List<Integer> quantities,
            String deliveryAddress) {

        Consumer consumer = consumerRepository.findById(consumerId)
                .orElseThrow(() -> new RuntimeException("Consumer not found"));

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        FoodOrder order = FoodOrder.builder()
                .consumer(consumer)
                .restaurant(restaurant)
                .status(OrderStatus.PLACED)
                .orderTime(LocalDateTime.now())
                .deliveryAddress(deliveryAddress)
                .lineItems(new ArrayList<>())
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < menuItemIds.size(); i++) {
            MenuItem menuItem = menuItemRepository.findById(menuItemIds.get(i))
                    .orElseThrow(() -> new RuntimeException("Menu item not found"));
            int qty = quantities.get(i);
            BigDecimal lineTotal = menuItem.getPrice().multiply(BigDecimal.valueOf(qty));
            total = total.add(lineTotal);

            OrderLineItem lineItem = OrderLineItem.builder()
                    .foodOrder(order)
                    .menuItem(menuItem)
                    .quantity(qty)
                    .price(lineTotal)
                    .build();
            order.getLineItems().add(lineItem);
        }
        order.setTotalAmount(total);

        FoodOrder savedOrder = foodOrderRepository.save(order);

        // Process payment (mock Stripe)
        paymentService.processPayment(savedOrder);

        // Generate bill
        billingService.generateBill(savedOrder);

        // Send notifications (mock Twilio SMS + SES email)
        notificationService.sendOrderConfirmation(savedOrder);

        // NOTE: Delivery is NOT created here. It is created only when
        // the restaurant marks the order as READY_FOR_PICKUP.

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

    public FoodOrder updateOrderStatus(Long orderId, OrderStatus newStatus) {
        FoodOrder order = getOrder(orderId);
        OrderStatus current = order.getStatus();

        // Enforce valid transitions
        switch (newStatus) {
            case APPROVED:
                if (current != OrderStatus.PLACED)
                    throw new RuntimeException("Can only approve a PLACED order");
                break;
            case PREPARING:
                if (current != OrderStatus.APPROVED)
                    throw new RuntimeException("Can only start preparing an APPROVED order");
                break;
            case READY_FOR_PICKUP:
                if (current != OrderStatus.PREPARING)
                    throw new RuntimeException("Can only mark ready a PREPARING order");
                break;
            case CANCELLED:
                if (current == OrderStatus.DELIVERED || current == OrderStatus.PICKED_UP)
                    throw new RuntimeException("Cannot cancel an order that is already picked up or delivered");
                break;
            default:
                break;
        }

        order.setStatus(newStatus);
        FoodOrder saved = foodOrderRepository.save(order);
        notificationService.sendOrderStatusUpdate(saved);
        return saved;
    }
}
