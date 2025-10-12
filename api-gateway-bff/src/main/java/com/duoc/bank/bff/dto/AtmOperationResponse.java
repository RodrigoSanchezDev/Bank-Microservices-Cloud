package com.duoc.bank.bff.dto;

import lombok.Builder;

/**
 * DTO para operaciones de cajeros automáticos (ATM).
 * Contiene solo información crítica para operaciones seguras y eficientes.
 */
@Builder
public record AtmOperationResponse(
    boolean success,
    String operationType,
    Double amount,
    Double newBalance,
    String receiptId,
    String timestamp,
    String atmId,
    SecurityInfo securityInfo,
    String error
) {
    public record SecurityInfo(
        String transactionId,
        String authorizationCode,
        String encryptedCardData
    ) {}
}

