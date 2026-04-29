package com.example.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AmazonSesAdapter {

    public boolean sendEmail(String to, String subject, String body) {
        // Mock Amazon SES email service
        log.info("[SES-MOCK] Sending email to {}, subject: {}, body: {}", to, subject, body);
        return true;
    }
}
