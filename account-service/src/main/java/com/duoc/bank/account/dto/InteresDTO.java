package com.duoc.bank.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO para datos de intereses del sistema legacy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InteresDTO {
    private Long cuentaId;
    private String nombre;
    private BigDecimal saldo;
    private Integer edad;
    private String tipo;
    private String semana;
    private boolean esValido;
    private String motivoInvalidez;
}
