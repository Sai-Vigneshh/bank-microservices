package com.bank.transaction.client;

import com.bank.transaction.dto.AccountResponse;
import com.bank.transaction.dto.UpdateBalanceRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "account-service")
public interface AccountClient {

    @PutMapping("/api/v1/accounts/{accountNumber}/balance")
    AccountResponse updateBalance(
            @PathVariable("accountNumber") String accountNumber,
            @RequestBody UpdateBalanceRequest request
    );

    @GetMapping("/api/v1/accounts/{accountNumber}")
    AccountResponse getAccountByNumber(
            @PathVariable("accountNumber") String accountNumber,
            @RequestHeader("X-User-Id") Long userId
    );
}