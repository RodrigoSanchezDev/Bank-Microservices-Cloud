package com.duoc.bank.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para datos de transacciones del sistema legacy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionDTO {
    private Long id;
    private LocalDate fecha;
    private BigDecimal monto;
    private String tipo;
    private String semana;
    private boolean esValido;
    private String motivoInvalidez;
}
