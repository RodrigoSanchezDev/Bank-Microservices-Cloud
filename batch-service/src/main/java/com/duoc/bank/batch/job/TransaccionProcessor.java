package com.duoc.bank.batch.job;

import com.duoc.bank.batch.model.TransaccionCSV;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class TransaccionProcessor implements ItemProcessor<TransaccionCSV, TransaccionCSV> {

    @Override
    public TransaccionCSV process(TransaccionCSV transaccion) throws Exception {
        // Detectar anomalías
        if (transaccion.getMonto() < 0) {
            log.warn("⚠️ Anomalía detectada: Monto negativo en transacción {}", transaccion.getId());
            transaccion.setEstado("ANOMALIA");
        } else if (transaccion.getMonto() > 1000000) {
            log.warn("⚠️ Anomalía detectada: Monto sospechoso alto en transacción {}", transaccion.getId());
            transaccion.setEstado("REVISION");
        } else {
            transaccion.setEstado("PROCESADA");
        }

        log.info("📊 Procesando transacción: ID={}, Tipo={}, Monto={}, Estado={}",
                transaccion.getId(), transaccion.getTipo(), transaccion.getMonto(), transaccion.getEstado());

        return transaccion;
    }
}
