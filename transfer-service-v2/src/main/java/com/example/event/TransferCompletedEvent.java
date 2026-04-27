package com.example.event;

import java.math.BigDecimal;
import org.springframework.context.ApplicationEvent;

public class TransferCompletedEvent extends ApplicationEvent {

    private final BigDecimal amount;
    private final String fromAccount;
    private final String toAccount;

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