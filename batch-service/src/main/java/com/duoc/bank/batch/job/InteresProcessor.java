package com.duoc.bank.batch.job;

import com.duoc.bank.batch.model.InteresCSV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * Processor para calcular y validar intereses mensuales
 */
@Component
public class InteresProcessor implements ItemProcessor<InteresCSV, InteresCSV> {

    private static final Logger logger = LoggerFactory.getLogger(InteresProcessor.class);

    @Override
    public InteresCSV process(InteresCSV interes) throws Exception {
        logger.info("Procesando interés ID: {} para cuenta: {}", interes.getId(), interes.getCuentaId());

        // Validar tasa de interés
        if (interes.getTasaInteres() < 0) {
            logger.warn("⚠️ Tasa de interés negativa detectada en cuenta {}: {}", 
                interes.getCuentaId(), interes.getTasaInteres());
            interes.setEstado("ERROR");
            return interes;
        }

        // Validar monto de interés
        if (interes.getMontoInteres() < 0) {
            logger.warn("⚠️ Monto de interés negativo detectado en cuenta {}: {}", 
                interes.getCuentaId(), interes.getMontoInteres());
            interes.setEstado("ERROR");
            return interes;
        }

        // Calcular y aplicar interés
        double montoCalculado = interes.getMontoInteres();
        
        // Validar montos altos
        if (montoCalculado > 10000) {
            logger.warn("⚠️ Monto de interés alto requiere revisión en cuenta {}: {}", 
                interes.getCuentaId(), montoCalculado);
            interes.setEstado("REVISION");
        } else {
            interes.setEstado("APLICADO");
        }

        logger.info("✅ Interés procesado: Cuenta {} - Tasa: {}% - Monto: {} - Estado: {}", 
            interes.getCuentaId(), interes.getTasaInteres(), 
            interes.getMontoInteres(), interes.getEstado());

        return interes;
    }
}
