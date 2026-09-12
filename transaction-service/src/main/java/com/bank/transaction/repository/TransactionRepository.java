package com.bank.transaction.repository;

import com.bank.transaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySourceAccountNumberOrTargetAccountNumberOrderByCreatedAtDesc(
            String source, String target
    );

}