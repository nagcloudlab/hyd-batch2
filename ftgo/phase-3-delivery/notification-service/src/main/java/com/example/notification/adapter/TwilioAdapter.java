package com.example.notification.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TwilioAdapter {

    public boolean sendSms(String phoneNumber, String message) {
        log.info("[TWILIO-MOCK] Sending SMS to {}: {}", phoneNumber, message);
        return true;
    }
}
