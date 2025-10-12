package com.duoc.bank.batch.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InteresCSV {
    private Long id;
    private Long cuentaId;
    private Double tasaInteres;
    private Double montoInteres;
    private String periodo;
    private String estado;
}
