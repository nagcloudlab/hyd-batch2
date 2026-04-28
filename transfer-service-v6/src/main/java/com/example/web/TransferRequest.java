package com.example.web;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// Form-backing object (DTO) — binds HTML form fields to Java object via @ModelAttribute
// Bean Validation annotations (@NotBlank, @NotNull, @DecimalMin) are checked when @Valid is used
// Validation errors are captured in BindingResult and displayed in the Thymeleaf template
public class TransferRequest {

    @NotBlank(message = "Please select the source account")
    private String fromAccount;

    @NotBlank(message = "Please select the destination account")
    private String toAccount;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be at least $0.01")
    private BigDecimal amount;

    public TransferRequest() {
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

}
