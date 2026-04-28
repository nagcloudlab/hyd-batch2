package com.example.web;

import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.service.TransferService;

@Controller
public class TransferController {

    private TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @RequestMapping(value = "/transfer", method = RequestMethod.GET)
    public String showTransferForm() {
        // authorize the user and show the transfer form
        return "transfer-form";
    }

    @RequestMapping(value = "/transfer", method = RequestMethod.POST)
    public String processTransfer(
            @RequestParam double amount,
            @RequestParam String fromAccount,
            @RequestParam String toAccount) {
        // convert the double amount to BigDecimal for better precision
        BigDecimal amountBD = BigDecimal.valueOf(amount);
        transferService.transfer(amountBD, fromAccount, toAccount);
        return "transfer-success";
    }

}
