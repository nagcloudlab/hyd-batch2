package com.example.api;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// DTO (Data Transfer Object) — decouples API contract from JPA entity
// @RequestBody + @Valid triggers Bean Validation before the controller method runs
@Getter
@Setter
public class TransferRequestDto {

    @NotBlank(message = "fromAccount is required")
    private String fromAccount;

    @NotBlank(message = "toAccount is required")
    private String toAccount;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be at least 0.01")
    private BigDecimal amount;

}
