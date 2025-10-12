package com.duoc.bank.batch.job;

import com.duoc.bank.batch.model.CuentaAnualCSV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * Processor para generar y validar estados de cuenta anuales
 */
@Component
public class EstadoCuentaProcessor implements ItemProcessor<CuentaAnualCSV, CuentaAnualCSV> {

    private static final Logger logger = LoggerFactory.getLogger(EstadoCuentaProcessor.class);

    @Override
    public CuentaAnualCSV process(CuentaAnualCSV cuenta) throws Exception {
        logger.info("Procesando estado de cuenta ID: {} - Cuenta: {} - Titular: {}", 
            cuenta.getId(), cuenta.getCuentaId(), cuenta.getTitular());

        // Validar saldos
        if (cuenta.getSaldoInicial() < 0) {
            logger.warn("⚠️ Saldo inicial negativo en cuenta {}: {}", 
                cuenta.getCuentaId(), cuenta.getSaldoInicial());
            cuenta.setEstado("ANOMALIA");
            return cuenta;
        }

        if (cuenta.getSaldoFinal() < 0) {
            logger.warn("⚠️ Saldo final negativo en cuenta {}: {}", 
                cuenta.getCuentaId(), cuenta.getSaldoFinal());
            cuenta.setEstado("ANOMALIA");
            return cuenta;
        }

        // Calcular variación anual
        double variacion = cuenta.getSaldoFinal() - cuenta.getSaldoInicial();
        double porcentajeVariacion = (cuenta.getSaldoInicial() != 0) 
            ? (variacion / cuenta.getSaldoInicial()) * 100 
            : 0;

        logger.info("📊 Variación anual: ${} ({}%)", 
            String.format("%.2f", variacion), 
            String.format("%.2f", porcentajeVariacion));

        // Validar movimientos sospechosos
        if (Math.abs(porcentajeVariacion) > 500) {
            logger.warn("⚠️ Variación anual inusual detectada en cuenta {}: {}%", 
                cuenta.getCuentaId(), String.format("%.2f", porcentajeVariacion));
            cuenta.setEstado("REVISION");
        } else if (cuenta.getTotalTransacciones() > 1000) {
            logger.warn("⚠️ Alto número de transacciones en cuenta {}: {}", 
                cuenta.getCuentaId(), cuenta.getTotalTransacciones());
            cuenta.setEstado("REVISION");
        } else {
            cuenta.setEstado("APROBADO");
        }

        logger.info("✅ Estado de cuenta procesado: {} - {} - Estado: {} - Transacciones: {}", 
            cuenta.getCuentaId(), cuenta.getTipoCuenta(), cuenta.getEstado(), 
            cuenta.getTotalTransacciones());

        return cuenta;
    }
}
