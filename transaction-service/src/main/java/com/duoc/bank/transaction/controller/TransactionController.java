package com.duoc.bank.transaction.controller;

import com.duoc.bank.transaction.model.Transaction;
import com.duoc.bank.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de transacciones
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Obtiene todas las transacciones
     */
    @GetMapping
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        log.info("GET /api/transactions - Obteniendo todas las transacciones");
        List<Transaction> transactions = transactionService.getAllTransactions();
        return ResponseEntity.ok(transactions);
    }

    /**
     * Obtiene una transacción por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Transaction> getTransactionById(@PathVariable Long id) {
        log.info("GET /api/transactions/{} - Obteniendo transacción", id);
        return transactionService.getTransactionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene transacciones por cuenta
     */
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Transaction>> getTransactionsByAccountId(@PathVariable Long accountId) {
        log.info("GET /api/transactions/account/{} - Obteniendo transacciones", accountId);
        List<Transaction> transactions = transactionService.getTransactionsByAccountId(accountId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Obtiene transacciones por cliente
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Transaction>> getTransactionsByCustomerId(@PathVariable Long customerId) {
        log.info("GET /api/transactions/customer/{} - Obteniendo transacciones", customerId);
        List<Transaction> transactions = transactionService.getTransactionsByCustomerId(customerId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Crea una nueva transacción
     */
    @PostMapping
    public ResponseEntity<Transaction> createTransaction(@RequestBody Transaction transaction) {
        log.info("POST /api/transactions - Creando nueva transacción");
        try {
            Transaction createdTransaction = transactionService.createTransaction(transaction);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
        } catch (Exception e) {
            log.error("Error al crear transacción: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Actualiza una transacción existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(@PathVariable Long id, @RequestBody Transaction transaction) {
        log.info("PUT /api/transactions/{} - Actualizando transacción", id);
        try {
            Transaction updatedTransaction = transactionService.updateTransaction(id, transaction);
            return ResponseEntity.ok(updatedTransaction);
        } catch (IllegalArgumentException e) {
            log.error("Error al actualizar transacción: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina una transacción
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable Long id) {
        log.info("DELETE /api/transactions/{} - Eliminando transacción", id);
        try {
            transactionService.deleteTransaction(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Error al eliminar transacción: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Transaction Service is running");
    }
}
