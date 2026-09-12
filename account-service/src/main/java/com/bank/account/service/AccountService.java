package com.bank.account.service;

import com.bank.account.dto.UpdateBalanceRequest;
import com.bank.account.dto.request.CreateAccountRequest;
import com.bank.account.dto.response.AccountResponse;
import com.bank.account.entity.Account;

import java.util.List;

public interface AccountService {

    AccountResponse createAccount(Long userId, CreateAccountRequest request);

    List<AccountResponse> getAccountsByUserId(Long userId);
    Account updateBalance(String accountNumber, UpdateBalanceRequest request);
    Account getAccountByNumber(String accountNumber);
    AccountResponse getAccountByAccountNumber(String accountNumber, Long userId);
}