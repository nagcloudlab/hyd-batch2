package com.example.accounting.web;

import com.example.accounting.entity.Bill;
import com.example.accounting.repository.BillRepository;
import com.example.accounting.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;

@Controller
@RequestMapping("/accounting")
@RequiredArgsConstructor
public class AccountingDashboardController {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;

    @GetMapping("")
    public String dashboard(Model model) {
        var payments = paymentRepository.findAll();
        var bills = billRepository.findAll();

        BigDecimal totalRevenue = bills.stream()
                .map(Bill::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTax = bills.stream()
                .map(Bill::getTax)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("payments", payments);
        model.addAttribute("bills", bills);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalTax", totalTax);
        return "accounting/dashboard";
    }
}
