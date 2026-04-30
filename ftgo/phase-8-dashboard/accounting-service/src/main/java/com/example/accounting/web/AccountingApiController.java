package com.example.accounting.web;

import com.example.accounting.entity.Bill;
import com.example.accounting.entity.Payment;
import com.example.accounting.service.AccountingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AccountingApiController {

    private final AccountingService accountingService;

    @GetMapping("/payments/order/{orderId}")
    public Payment getPayment(@PathVariable Long orderId) {
        return accountingService.getPaymentByOrder(orderId);
    }

    @GetMapping("/bills/order/{orderId}")
    public Bill getBill(@PathVariable Long orderId) {
        return accountingService.getBillByOrder(orderId);
    }
}
