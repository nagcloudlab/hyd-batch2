package com.example.notification.consumer;

import com.example.notification.event.NotificationEvent;
import com.example.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "ftgo.notification.events", groupId = "notification-service")
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("[KAFKA-CONSUMER] Received event: {}", event);

        // simulate processing time
        try {
            Thread.sleep(3);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        switch (event.getEventType()) {
            case "ORDER_CONFIRMED" -> handleOrderConfirmed(event);
            case "ORDER_STATUS_UPDATED" -> handleOrderStatusUpdated(event);
            case "DELIVERY_ASSIGNED" -> handleDeliveryAssigned(event);
            case "DELIVERY_PICKED_UP" -> handleDeliveryPickedUp(event);
            case "DELIVERY_COMPLETED" -> handleDeliveryCompleted(event);
            default -> log.warn("Unknown event type: {}", event.getEventType());
        }
    }

    private void handleOrderConfirmed(NotificationEvent event) {
        String message = "Order #" + event.getOrderId() + " placed at " + event.getRestaurantName()
                + ". Total: Rs." + event.getTotalAmount();

        // SMS to consumer
        notificationService.sendSms(event.getConsumerPhone(), message);

        // Email to consumer
        notificationService.sendEmail(event.getConsumerEmail(),
                "Order Confirmation - #" + event.getOrderId(), message);

        // SMS to restaurant
        notificationService.sendSms(event.getRestaurantPhone(),
                "New order #" + event.getOrderId() + " received! Total: Rs." + event.getTotalAmount());
    }

    private void handleOrderStatusUpdated(NotificationEvent event) {
        String message = "Order #" + event.getOrderId() + " status updated to: " + event.getOrderStatus();

        notificationService.sendSms(event.getConsumerPhone(), message);
        notificationService.sendEmail(event.getConsumerEmail(),
                "Order Update - #" + event.getOrderId(), message);
    }

    private void handleDeliveryAssigned(NotificationEvent event) {
        notificationService.sendSms(event.getConsumerPhone(),
                "Your order #" + event.getOrderId() + " has been assigned to courier " + event.getCourierName());
        notificationService.sendSms(event.getRestaurantPhone(),
                "Courier " + event.getCourierName() + " is on the way to pick up order #" + event.getOrderId());
    }

    private void handleDeliveryPickedUp(NotificationEvent event) {
        notificationService.sendSms(event.getConsumerPhone(),
                "Your order #" + event.getOrderId() + " has been picked up and is on the way!");
    }

    private void handleDeliveryCompleted(NotificationEvent event) {
        notificationService.sendSms(event.getConsumerPhone(),
                "Your order #" + event.getOrderId() + " has been delivered. Enjoy your meal!");
    }
}
