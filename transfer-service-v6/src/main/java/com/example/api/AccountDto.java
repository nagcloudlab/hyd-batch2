package com.example.api;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// DTO for Account — used for both request (POST/PUT) and response
// Never expose JPA entities directly in API responses (leaks DB schema)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {

    @NotBlank(message = "number is required")
    private String number;

    @NotNull(message = "balance is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "balance cannot be negative")
    private BigDecimal balance;

}
