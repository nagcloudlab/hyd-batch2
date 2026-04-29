package com.example.service;

import com.example.entity.Bill;
import com.example.entity.FoodOrder;
import com.example.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class BillingService {

    private final BillRepository billRepository;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.05");        // 5% GST
    private static final BigDecimal DELIVERY_FEE = new BigDecimal("30.00");   // Rs.30

    public Bill generateBill(FoodOrder order) {
        BigDecimal amount = order.getTotalAmount();
        BigDecimal tax = amount.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = amount.add(tax).add(DELIVERY_FEE);

        Bill bill = Bill.builder()
                .foodOrder(order)
                .amount(amount)
                .tax(tax)
                .deliveryFee(DELIVERY_FEE)
                .totalAmount(total)
                .generatedTime(LocalDateTime.now())
                .build();

        return billRepository.save(bill);
    }

    public Bill getBillByOrder(Long orderId) {
        return billRepository.findByFoodOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Bill not found for order: " + orderId));
    }
}
