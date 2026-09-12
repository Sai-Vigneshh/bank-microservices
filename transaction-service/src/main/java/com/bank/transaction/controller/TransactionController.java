package com.bank.transaction.controller;

import com.bank.transaction.dto.DepositRequest;
import com.bank.transaction.dto.TransactionResponse;
import com.bank.transaction.dto.TransferRequest;
import com.bank.transaction.dto.WithdrawRequest;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.repository.TransactionRepository;
import com.bank.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionRepository transactionRepository;

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@RequestBody DepositRequest request) {
        TransactionResponse response = transactionService.deposit(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@RequestHeader("X-User-Id") Long userId,@RequestBody WithdrawRequest request) {
        TransactionResponse response = transactionService.withdraw(request,userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@RequestHeader("X-User-Id") Long userId,@RequestBody TransferRequest request) {
        TransactionResponse response = transactionService.transfer(request,userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/accounts/{accountNumber}/statement")
    public ResponseEntity<List<Transaction>> getAccountStatement(@PathVariable String accountNumber) {
        List<Transaction> transactions = transactionRepository
                .findBySourceAccountNumberOrTargetAccountNumberOrderByCreatedAtDesc(accountNumber, accountNumber);
        return ResponseEntity.ok(transactions);
    }
}