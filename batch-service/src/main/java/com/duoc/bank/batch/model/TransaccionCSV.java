package com.duoc.bank.batch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionCSV {
    private Long id;
    private String tipo;
    private Double monto;
    private String fecha;
    private Long cuentaId;
    private String descripcion;
    private String estado;
}
