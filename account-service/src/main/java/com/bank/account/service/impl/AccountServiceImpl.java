package com.bank.account.service.impl;

import com.bank.account.dto.UpdateBalanceRequest;
import com.bank.account.dto.request.CreateAccountRequest;
import com.bank.account.dto.response.AccountResponse;
import com.bank.account.entity.Account;
import com.bank.account.entity.AccountStatus;
import com.bank.account.repository.AccountRepository;
import com.bank.account.service.AccountService;
import com.bank.account.util.AccountNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountNumberGenerator accountNumberGenerator;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public AccountResponse createAccount(Long userId, CreateAccountRequest request) {
        String accountNumber = accountNumberGenerator.generateUniqueAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .userId(userId)
                .accountType(request.getAccountType())
                .balance(request.getInitialDeposit())
                .status(AccountStatus.ACTIVE)
                .build();

        Account savedAccount = accountRepository.save(account);
        return mapToResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByUserId(Long userId) {
        return accountRepository.findByUserId(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByAccountNumber(String accountNumber, Long userId) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found with number: " + accountNumber));

        if (!account.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied: You do not own this account");
        }

        return mapToResponse(account);
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .accountNumber(account.getAccountNumber())
                .userId(account.getUserId())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }
    @Override
    @Transactional
    public Account updateBalance(String accountNumber, UpdateBalanceRequest request) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found with number: " + accountNumber));

        if ("DEBIT".equalsIgnoreCase(request.getOperation())) {
            if (request.getUserId() != null && !account.getUserId().equals(request.getUserId())) {
                throw new RuntimeException("Access Denied: You do not own this account");
            }
            if (account.getBalance().compareTo(request.getAmount()) < 0) {
                throw new RuntimeException("Insufficient balance");
            }
            account.setBalance(account.getBalance().subtract(request.getAmount()));
        } else if ("CREDIT".equalsIgnoreCase(request.getOperation())) {
            account.setBalance(account.getBalance().add(request.getAmount()));
        } else {
            throw new RuntimeException("Invalid operation: " + request.getOperation());
        }

        return accountRepository.save(account);
    }

    @Override
    public Account getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found with number: " + accountNumber));
    }
}
