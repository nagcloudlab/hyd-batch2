package com.example.notification.service;

import com.example.notification.adapter.AmazonSesAdapter;
import com.example.notification.adapter.TwilioAdapter;
import com.example.notification.entity.Notification;
import com.example.notification.entity.NotificationType;
import com.example.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final TwilioAdapter twilioAdapter;
    private final AmazonSesAdapter amazonSesAdapter;

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
