package com.example.api;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TransferRequestDto {

    private String fromAccount;
    private String toAccount;
    private double amount;

}
