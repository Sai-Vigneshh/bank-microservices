package com.bank.transaction.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    private String sourceAccountNumber;
    private String targetAccountNumber;
    private BigDecimal amount;
    private String description;
}