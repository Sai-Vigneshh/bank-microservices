package com.bank.transaction.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class WithdrawRequest {
    private String sourceAccountNumber;
    private BigDecimal amount;
    private String description;
}