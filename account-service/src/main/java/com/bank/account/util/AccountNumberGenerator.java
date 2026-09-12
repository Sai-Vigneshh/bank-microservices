package com.bank.account.util;
import com.bank.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
@RequiredArgsConstructor
public class AccountNumberGenerator {
    private final   AccountRepository accountRepository;
    private final SecureRandom secureRandom=new SecureRandom();
    public String generateUniqueAccountNumber()
    {
        String accountNumber;
        do {
            accountNumber = generateRandom10DigitString();
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
    private  String generateRandom10DigitString()
    {
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append(secureRandom.nextInt(9) + 1);
        for(int i=0;i<9;i++)
        {
            stringBuilder.append(secureRandom.nextInt(10));
        }
        return stringBuilder.toString();

    }
}