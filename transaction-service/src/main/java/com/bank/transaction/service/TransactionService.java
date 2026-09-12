package com.bank.transaction.service;

import com.bank.transaction.client.AccountClient;
import com.bank.transaction.dto.*;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.entity.TransactionStatus;
import com.bank.transaction.entity.TransactionType;
import com.bank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;

    public TransactionResponse deposit(DepositRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than 0");
        }

        // క్రెడిట్ కి userId అవసరం లేదు (null)
        AccountResponse accountResponse = accountClient.updateBalance(
                request.getTargetAccountNumber(),
                new UpdateBalanceRequest(request.getAmount(), "CREDIT", null)
        );

        String transactionId = UUID.randomUUID().toString();
        Transaction transaction = Transaction.builder()
                .transactionReference(transactionId)
                .sourceAccountNumber(null)
                .targetAccountNumber(request.getTargetAccountNumber())
                .amount(request.getAmount())
                .transactionType(TransactionType.DEPOSIT)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        return TransactionResponse.builder()
                .transactionReference(transaction.getTransactionReference())
                .sourceAccountNumber(null)
                .targetAccountNumber(transaction.getTargetAccountNumber())
                .amount(transaction.getAmount())
                .type(transaction.getTransactionType())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .remainingBalance(accountResponse.getBalance())
                .timestamp(transaction.getCreatedAt())
                .build();
    }

    // 1. Long userId ని పారామీటర్‌గా చేర్చాం
    public TransactionResponse withdraw(WithdrawRequest request, Long userId) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than 0");
        }

        AccountResponse accountResponse = accountClient.updateBalance(
                request.getSourceAccountNumber(),
                new UpdateBalanceRequest(request.getAmount(), "DEBIT", userId)
        );

        String transactionId = UUID.randomUUID().toString();
        Transaction transaction = Transaction.builder()
                .transactionReference(transactionId)
                .sourceAccountNumber(request.getSourceAccountNumber())
                .targetAccountNumber(null)
                .amount(request.getAmount())
                .transactionType(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        return TransactionResponse.builder()
                .transactionReference(transaction.getTransactionReference())
                .sourceAccountNumber(transaction.getSourceAccountNumber())
                .targetAccountNumber(null)
                .amount(transaction.getAmount())
                .type(transaction.getTransactionType())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .remainingBalance(accountResponse.getBalance())
                .timestamp(transaction.getCreatedAt())
                .build();
    }

    public TransactionResponse transfer(TransferRequest request, Long userId) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than 0");
        }

        if (request.getSourceAccountNumber().equals(request.getTargetAccountNumber())) {
            throw new RuntimeException("Source and target account numbers cannot be the same");
        }

        // సోర్స్ ఖాతాకు userId తో డెబిట్
        AccountResponse senderAccount = accountClient.updateBalance(
                request.getSourceAccountNumber(),
                new UpdateBalanceRequest(request.getAmount(), "DEBIT", userId)
        );

        // 2. టార్గెట్ ఖాతాకు క్రెడిట్ చేసేటప్పుడు userId null ఉండాలి
        accountClient.updateBalance(
                request.getTargetAccountNumber(),
                new UpdateBalanceRequest(request.getAmount(), "CREDIT", null)
        );

        String transactionId = UUID.randomUUID().toString();
        Transaction transaction = Transaction.builder()
                .transactionReference(transactionId)
                .sourceAccountNumber(request.getSourceAccountNumber())
                .targetAccountNumber(request.getTargetAccountNumber())
                .amount(request.getAmount())
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.SUCCESS)
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);

        return TransactionResponse.builder()
                .transactionReference(transaction.getTransactionReference())
                .sourceAccountNumber(transaction.getSourceAccountNumber())
                .targetAccountNumber(transaction.getTargetAccountNumber())
                .amount(transaction.getAmount())
                .type(transaction.getTransactionType())
                .status(transaction.getStatus())
                .description(transaction.getDescription())
                .remainingBalance(senderAccount.getBalance())
                .timestamp(transaction.getCreatedAt())
                .build();
    }
}