package com.example.api;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.service.TransferService;

@RestController
@RequestMapping("/api/v1/transfer")
public class TransferRestApiController {

    private final TransferService transferService;

    public TransferRestApiController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public TransferResponseDto doTransfer(@RequestBody TransferRequestDto request) {
        BigDecimal amount = BigDecimal.valueOf(request.getAmount());
        transferService.transfer(amount, request.getFromAccount(), request.getToAccount());
        TransferResponseDto response = new TransferResponseDto();
        response.setTransactionId("123abc");
        return response;
    }

}

/*
 * 
 * curl -X POST http://localhost:8080/api/v1/transfer \
 * -H "Content-Type: application/json" \
 * -d '{
 * "fromAccount": "123",
 * "toAccount": "456",
 * "amount": 100.00
 * }'
 * 
 */