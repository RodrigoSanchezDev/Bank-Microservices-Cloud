package com.duoc.bank.batch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CuentaAnualCSV {
    private Long id;
    private Long cuentaId;
    private String titular;
    private String tipoCuenta;
    private Double saldoInicial;
    private Double saldoFinal;
    private Integer totalTransacciones;
    private String periodo;
    private String estado;
}
