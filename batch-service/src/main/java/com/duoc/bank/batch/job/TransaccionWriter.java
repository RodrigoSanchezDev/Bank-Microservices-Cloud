package com.duoc.bank.batch.job;

import com.duoc.bank.batch.model.TransaccionCSV;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TransaccionWriter implements ItemWriter<TransaccionCSV> {

    private Map<String, Integer> resumen = new HashMap<>();
    private double totalMonto = 0.0;
    private int totalAnomalias = 0;

    @Override
    public void write(Chunk<? extends TransaccionCSV> chunk) throws Exception {
        for (TransaccionCSV transaccion : chunk) {
            // Actualizar resumen
            String tipo = transaccion.getTipo();
            resumen.put(tipo, resumen.getOrDefault(tipo, 0) + 1);
            totalMonto += transaccion.getMonto();

            if ("ANOMALIA".equals(transaccion.getEstado()) || "REVISION".equals(transaccion.getEstado())) {
                totalAnomalias++;
            }

            log.info("💾 Guardando transacción: ID={}, Tipo={}, Monto={}, Estado={}",
                    transaccion.getId(), transaccion.getTipo(), transaccion.getMonto(), transaccion.getEstado());
        }
    }

    public void imprimirResumen() {
        log.info("=".repeat(60));
        log.info("📊 RESUMEN DE TRANSACCIONES DIARIAS");
        log.info("=".repeat(60));
        log.info("Total de transacciones procesadas: {}", resumen.values().stream().mapToInt(Integer::intValue).sum());
        log.info("Total monto procesado: ${}", String.format("%.2f", totalMonto));
        log.info("Total anomalías detectadas: {}", totalAnomalias);
        log.info("Transacciones por tipo:");
        resumen.forEach((tipo, cantidad) -> log.info("  - {}: {}", tipo, cantidad));
        log.info("=".repeat(60));
    }
}
