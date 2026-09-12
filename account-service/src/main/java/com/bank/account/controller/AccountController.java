package com.bank.account.controller;

import com.bank.account.dto.UpdateBalanceRequest;
import com.bank.account.dto.request.CreateAccountRequest;
import com.bank.account.dto.response.AccountResponse;
import com.bank.account.entity.Account;
import com.bank.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/accounts")
public class AccountController {
    private final AccountService accountService;
    @GetMapping("/my-accounts")
    public ResponseEntity<List<AccountResponse>> getMyAccounts(
            @RequestHeader("X-User-Id") Long userId
    ) {
        List<AccountResponse> accounts = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(accounts);
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateAccountRequest request
    ) {
        AccountResponse response = accountService.createAccount(userId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountByNumber(
            @PathVariable String accountNumber,
            @RequestHeader("X-User-Id") Long userId
    ) {
        AccountResponse response = accountService.getAccountByAccountNumber(accountNumber, userId);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/{accountNumber}/balance")
    public ResponseEntity<Account> updateBalance(
            @PathVariable String accountNumber,
            @RequestBody UpdateBalanceRequest request) {
        Account updatedAccount = accountService.updateBalance(accountNumber, request);
        return ResponseEntity.ok(updatedAccount);
    }

//    @GetMapping("/{accountNumber}")
//    public ResponseEntity<Account> getAccountByNumber(@PathVariable String accountNumber) {
//        Account account = accountService.getAccountByNumber(accountNumber);
//        return ResponseEntity.ok(account);
//    }
}