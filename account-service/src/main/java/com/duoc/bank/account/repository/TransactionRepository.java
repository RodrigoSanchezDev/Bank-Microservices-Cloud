package com.duoc.bank.account.repository;

import com.duoc.bank.account.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByAccountId(Long accountId);
    
    List<Transaction> findByAccountIdOrderByTransactionDateDesc(Long accountId);
}
