package com.bank.transaction.dto;

import com.bank.transaction.entity.TransactionStatus;
import com.bank.transaction.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private String transactionReference;
    private String sourceAccountNumber;
    private String targetAccountNumber;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private String description;
    private BigDecimal remainingBalance;
    private LocalDateTime timestamp;
}