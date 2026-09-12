package com.bank.account.repository;
import com.bank.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<com.bank.account.entity.Account, Long> {


    List<com.bank.account.entity.Account> findByUserId(Long userId);
    Optional<Account> findByAccountNumber(String accountNumber);
    boolean existsByAccountNumber(String accountNumber);
}