package com.duoc.bank.bff.dto;

import lombok.Builder;
import java.time.LocalDateTime;

/**
 * DTO completo para clientes web.
 * Incluye todos los detalles necesarios para interfaces ricas y complejas.
 */
@Builder
public record WebAccountResponse(
    Long id,
    String accountNumber,
    String accountType,
    Double balance,
    Double availableBalance,
    String currency,
    String status,
    LocalDateTime createdAt,
    LocalDateTime lastActivity,
    CustomerSummary customer,
    AccountLimits limits,
    AccountMetrics metrics
) {
    public record CustomerSummary(
        Long id,
        String fullName,
        String email,
        String phone,
        String segment
    ) {}
    
    public record AccountLimits(
        Double dailyLimit,
        Double monthlyLimit,
        Double transferLimit
    ) {}
    
    public record AccountMetrics(
        Integer transactionCount,
        Double avgTransactionAmount,
        Integer activeCards,
        Boolean hasOverdraft
    ) {}
}
