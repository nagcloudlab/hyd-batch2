package com.example.service;

import com.example.adapter.AmazonSesAdapter;
import com.example.adapter.TwilioAdapter;
import com.example.entity.FoodOrder;
import com.example.entity.Notification;
import com.example.entity.NotificationType;
import com.example.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final TwilioAdapter twilioAdapter;
    private final AmazonSesAdapter amazonSesAdapter;

    public void sendOrderConfirmation(FoodOrder order) {
        String message = "Order #" + order.getId() + " placed at " + order.getRestaurant().getName()
                + ". Total: Rs." + order.getTotalAmount();

        // SMS to consumer
        sendSms(order.getConsumer().getPhone(), message);

        // Email to consumer
        sendEmail(order.getConsumer().getEmail(), "Order Confirmation - #" + order.getId(), message);

        // SMS to restaurant
        sendSms(order.getRestaurant().getPhone(),
                "New order #" + order.getId() + " received! Total: Rs." + order.getTotalAmount());
    }

    public void sendOrderStatusUpdate(FoodOrder order) {
        String message = "Order #" + order.getId() + " status updated to: " + order.getStatus();
        sendSms(order.getConsumer().getPhone(), message);
        sendEmail(order.getConsumer().getEmail(), "Order Update - #" + order.getId(), message);
    }

    public void sendSms(String phone, String message) {
        twilioAdapter.sendSms(phone, message);

        Notification notification = Notification.builder()
                .type(NotificationType.SMS)
                .recipient(phone)
                .message(message)
                .sent(true)
                .sentTime(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    public void sendEmail(String email, String subject, String body) {
        amazonSesAdapter.sendEmail(email, subject, body);

        Notification notification = Notification.builder()
                .type(NotificationType.EMAIL)
                .recipient(email)
                .subject(subject)
                .message(body)
                .sent(true)
                .sentTime(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }
}
