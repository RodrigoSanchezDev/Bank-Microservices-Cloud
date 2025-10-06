package com.duoc.bank.transaction.service;

import com.duoc.bank.transaction.model.Transaction;
import com.duoc.bank.transaction.repository.TransactionRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de transacciones con patrones de resiliencia
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    /**
     * Obtiene todas las transacciones
     */
    @CircuitBreaker(name = "transactionService", fallbackMethod = "getAllTransactionsFallback")
    @Retry(name = "transactionService")
    public List<Transaction> getAllTransactions() {
        log.info("Obteniendo todas las transacciones");
        return transactionRepository.findAll();
    }

    /**
     * Obtiene una transacción por ID
     */
    @CircuitBreaker(name = "transactionService", fallbackMethod = "getTransactionByIdFallback")
    @Retry(name = "transactionService")
    public Optional<Transaction> getTransactionById(Long id) {
        log.info("Obteniendo transacción con ID: {}", id);
        return transactionRepository.findById(id);
    }

    /**
     * Obtiene transacciones por cuenta
     */
    @CircuitBreaker(name = "transactionService", fallbackMethod = "getTransactionsByAccountIdFallback")
    @Retry(name = "transactionService")
    public List<Transaction> getTransactionsByAccountId(Long accountId) {
        log.info("Obteniendo transacciones para cuenta: {}", accountId);
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    /**
     * Obtiene transacciones por cliente
     */
    @CircuitBreaker(name = "transactionService", fallbackMethod = "getTransactionsByCustomerIdFallback")
    @Retry(name = "transactionService")
    public List<Transaction> getTransactionsByCustomerId(Long customerId) {
        log.info("Obteniendo transacciones para cliente: {}", customerId);
        return transactionRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    /**
     * Crea una nueva transacción
     */
    @Transactional
    @CircuitBreaker(name = "transactionService", fallbackMethod = "createTransactionFallback")
    @Retry(name = "transactionService")
    public Transaction createTransaction(Transaction transaction) {
        log.info("Creando nueva transacción para cuenta: {}", transaction.getAccountId());
        return transactionRepository.save(transaction);
    }

    /**
     * Actualiza una transacción existente
     */
    @Transactional
    @CircuitBreaker(name = "transactionService", fallbackMethod = "updateTransactionFallback")
    @Retry(name = "transactionService")
    public Transaction updateTransaction(Long id, Transaction transactionDetails) {
        log.info("Actualizando transacción con ID: {}", id);
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada con ID: " + id));
        
        transaction.setStatus(transactionDetails.getStatus());
        transaction.setDescription(transactionDetails.getDescription());
        
        return transactionRepository.save(transaction);
    }

    /**
     * Elimina una transacción
     */
    @Transactional
    @CircuitBreaker(name = "transactionService", fallbackMethod = "deleteTransactionFallback")
    @Retry(name = "transactionService")
    public void deleteTransaction(Long id) {
        log.info("Eliminando transacción con ID: {}", id);
        
        if (!transactionRepository.existsById(id)) {
            throw new IllegalArgumentException("Transacción no encontrada con ID: " + id);
        }
        
        transactionRepository.deleteById(id);
    }

    // Fallback methods

    private List<Transaction> getAllTransactionsFallback(Exception e) {
        log.error("Error al obtener transacciones, retornando lista vacía. Error: {}", e.getMessage());
        return List.of();
    }

    private Optional<Transaction> getTransactionByIdFallback(Long id, Exception e) {
        log.error("Error al obtener transacción con ID: {}. Error: {}", id, e.getMessage());
        return Optional.empty();
    }

    private List<Transaction> getTransactionsByAccountIdFallback(Long accountId, Exception e) {
        log.error("Error al obtener transacciones para cuenta: {}. Error: {}", accountId, e.getMessage());
        return List.of();
    }

    private List<Transaction> getTransactionsByCustomerIdFallback(Long customerId, Exception e) {
        log.error("Error al obtener transacciones para cliente: {}. Error: {}", customerId, e.getMessage());
        return List.of();
    }

    private Transaction createTransactionFallback(Transaction transaction, Exception e) {
        log.error("Error al crear transacción. Error: {}", e.getMessage());
        throw new RuntimeException("No se pudo crear la transacción en este momento. Por favor, intente más tarde.", e);
    }

    private Transaction updateTransactionFallback(Long id, Transaction transaction, Exception e) {
        log.error("Error al actualizar transacción con ID: {}. Error: {}", id, e.getMessage());
        throw new RuntimeException("No se pudo actualizar la transacción en este momento. Por favor, intente más tarde.", e);
    }

    private void deleteTransactionFallback(Long id, Exception e) {
        log.error("Error al eliminar transacción con ID: {}. Error: {}", id, e.getMessage());
        throw new RuntimeException("No se pudo eliminar la transacción en este momento. Por favor, intente más tarde.", e);
    }
}
