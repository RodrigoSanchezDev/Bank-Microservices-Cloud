package com.duoc.bank.bff.dto;

/**
 * DTO optimizado para clientes móviles.
 * Contiene solo los datos esenciales para reducir el consumo de ancho de banda.
 */
public record MobileAccountResponse(
    Long id,
    String accountNumber,
    Double balance,
    String status
) {}
