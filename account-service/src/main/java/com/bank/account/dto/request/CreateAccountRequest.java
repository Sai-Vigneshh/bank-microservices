package com.bank.account.dto.request;

import com.bank.account.entity.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAccountRequest {
    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @NotNull(message = "Initial deposit is required")
    @PositiveOrZero(message = "Initial deposit cannot be negative")
    private BigDecimal initialDeposit;

}