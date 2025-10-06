package com.duoc.bank.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para datos de cuentas anuales del sistema legacy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CuentaAnualDTO {
    private Long cuentaId;
    private LocalDate fecha;
    private String transaccion;
    private BigDecimal monto;
    private String descripcion;
    private String semana;
    private boolean esValido;
    private String motivoInvalidez;
}
