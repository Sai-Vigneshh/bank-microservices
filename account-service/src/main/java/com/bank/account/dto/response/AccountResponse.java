package com.bank.account.dto.response;

import com.bank.account.entity.AccountStatus;
import com.bank.account.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private String accountNumber;
    private Long userId;
    private AccountType accountType;
    private BigDecimal balance;
    private AccountStatus status;
    private LocalDateTime createdAt;
}