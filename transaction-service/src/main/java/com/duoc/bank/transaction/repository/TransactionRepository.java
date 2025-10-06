package com.duoc.bank.transaction.repository;

import com.duoc.bank.transaction.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la entidad Transaction
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    List<Transaction> findByAccountId(Long accountId);
    
    List<Transaction> findByCustomerId(Long customerId);
    
    List<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);
    
    List<Transaction> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
