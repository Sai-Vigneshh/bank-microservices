package com.bank.transaction.service;

import com.bank.transaction.client.AccountClient;
import com.bank.transaction.dto.*;
import com.bank.transaction.entity.Transaction;
import com.bank.transaction.entity.TransactionStatus;
import com.bank.transaction.entity.TransactionType;
import com.bank.transaction.exception.GlobalExceptionHandler.TransactionExecutionException;
import com.bank.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;

    public TransactionResponse deposit(DepositRequest request) {
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be greater than 0");
        }

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

        String transactionId = UUID.randomUUID().toString();
        Transaction transaction = Transaction.builder()
                .transactionReference(transactionId)
                .sourceAccountNumber(request.getSourceAccountNumber())
                .targetAccountNumber(request.getTargetAccountNumber())
                .amount(request.getAmount())
                .transactionType(TransactionType.TRANSFER)
                .status(TransactionStatus.PENDING)
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .build();

        transaction = transactionRepository.save(transaction);

        UpdateBalanceRequest debitRequest = UpdateBalanceRequest.builder()
                .amount(request.getAmount())
                .operation("DEBIT")
                .userId(userId)
                .build();

        AccountResponse debitResponse;
        try {
            debitResponse = accountClient.updateBalance(request.getSourceAccountNumber(), debitRequest);
            log.info("Debit successful for account: {}, TxRef: {}", request.getSourceAccountNumber(), transactionId);
        } catch (Exception ex) {
            log.error("Debit failed for source account: {}. Aborting transfer.", request.getSourceAccountNumber(), ex);
            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            throw ex;
        }

        UpdateBalanceRequest creditRequest = UpdateBalanceRequest.builder()
                .amount(request.getAmount())
                .operation("CREDIT")
                .userId(null)
                .build();

        try {
            accountClient.updateBalance(request.getTargetAccountNumber(), creditRequest);
            log.info("Credit successful for target account: {}, TxRef: {}", request.getTargetAccountNumber(), transactionId);

            transaction.setStatus(TransactionStatus.SUCCESS);
            transactionRepository.save(transaction);

            return TransactionResponse.builder()
                    .transactionReference(transaction.getTransactionReference())
                    .sourceAccountNumber(transaction.getSourceAccountNumber())
                    .targetAccountNumber(transaction.getTargetAccountNumber())
                    .amount(transaction.getAmount())
                    .type(transaction.getTransactionType())
                    .status(transaction.getStatus())
                    .description(transaction.getDescription())
                    .remainingBalance(debitResponse.getBalance())
                    .timestamp(transaction.getCreatedAt())
                    .build();

        } catch (Exception ex) {
            log.error("Credit failed for target account: {}. Initiating compensation refund to source account: {}",
                    request.getTargetAccountNumber(), request.getSourceAccountNumber(), ex);

            executeCompensationRefund(request.getSourceAccountNumber(), request.getAmount(), transactionId);

            transaction.setStatus(TransactionStatus.REVERSED);
            transactionRepository.save(transaction);

            throw new TransactionExecutionException("Transfer failed during credit step. Deducted funds have been refunded to source account.");
        }
    }

    private void executeCompensationRefund(String sourceAccountNumber, BigDecimal amount, String transactionId) {
        try {
            UpdateBalanceRequest refundRequest = UpdateBalanceRequest.builder()
                    .amount(amount)
                    .operation("CREDIT")
                    .userId(null)
                    .build();

            accountClient.updateBalance(sourceAccountNumber, refundRequest);
            log.warn("Compensation completed: Successfully refunded {} to account: {} for TxRef: {}", amount, sourceAccountNumber, transactionId);
        } catch (Exception refundEx) {
            log.error("CRITICAL: Compensation refund failed for account: {}! Manual intervention required. TxRef: {}",
                    sourceAccountNumber, transactionId, refundEx);
        }
    }
}