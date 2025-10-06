package com.duoc.bank.account.model;

/**
 * Estados de una transacción
 */
public enum TransactionStatus {
    PENDING,        // Pendiente
    COMPLETED,      // Completada
    FAILED,         // Fallida
    CANCELLED       // Cancelada
}
