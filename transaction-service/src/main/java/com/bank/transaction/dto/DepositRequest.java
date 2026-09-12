package com.bank.transaction.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class DepositRequest {
    private String targetAccountNumber;
    private BigDecimal amount;
    private String description;
}