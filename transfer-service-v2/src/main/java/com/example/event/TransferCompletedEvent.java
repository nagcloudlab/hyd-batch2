package com.example.event;

import java.math.BigDecimal;

import org.springframework.context.ApplicationEvent;

// Custom Spring event — published after a successful transfer
// Extends ApplicationEvent so ApplicationEventPublisher.publishEvent() can route it
// Fields are final — events are immutable data carriers (created once, read by listeners)
// Modern alternative (Spring 4.2+): plain POJO events without extending ApplicationEvent
public class TransferCompletedEvent extends ApplicationEvent {

    private final BigDecimal amount;
    private final String fromAccount;
    private final String toAccount;

    // 'source' — the object that published this event (typically the service bean)
    public TransferCompletedEvent(Object source, BigDecimal amount, String fromAccount, String toAccount) {
        super(source);
        this.amount = amount;
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

}
