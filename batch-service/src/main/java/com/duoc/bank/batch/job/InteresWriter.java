package com.duoc.bank.batch.job;

import com.duoc.bank.batch.model.InteresCSV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Writer para guardar y generar resumen de intereses aplicados
 */
@Component
public class InteresWriter implements ItemWriter<InteresCSV> {

    private static final Logger logger = LoggerFactory.getLogger(InteresWriter.class);
    
    private double totalIntereses = 0.0;
    private int totalAplicados = 0;
    private int totalRevisiones = 0;
    private int totalErrores = 0;
    private Map<Long, Double> interesesPorCuenta = new HashMap<>();

    @Override
    public void write(Chunk<? extends InteresCSV> chunk) throws Exception {
        for (InteresCSV interes : chunk) {
            logger.info("💰 Guardando interés: Cuenta {} - Monto: {} - Estado: {}", 
                interes.getCuentaId(), interes.getMontoInteres(), interes.getEstado());

            // Acumular estadísticas
            totalIntereses += interes.getMontoInteres();
            
            // Acumular por cuenta
            interesesPorCuenta.merge(interes.getCuentaId(), interes.getMontoInteres(), Double::sum);
            
            // Contar por estado
            switch (interes.getEstado()) {
                case "APLICADO":
                    totalAplicados++;
                    break;
                case "REVISION":
                    totalRevisiones++;
                    break;
                case "ERROR":
                    totalErrores++;
                    break;
            }
        }
        
        // Imprimir resumen parcial
        logger.info("📊 Resumen parcial - Total intereses: {} - Aplicados: {} - Revisiones: {} - Errores: {}", 
            totalIntereses, totalAplicados, totalRevisiones, totalErrores);
    }

    public void imprimirResumen() {
        logger.info("\n" +
                "╔════════════════════════════════════════════════════════════╗\n" +
                "║         RESUMEN DE CÁLCULO DE INTERESES MENSUALES          ║\n" +
                "╠════════════════════════════════════════════════════════════╣\n" +
                "║ Total Intereses Calculados: ${} \n" +
                "║ Total Registros Aplicados: {} \n" +
                "║ Total Requieren Revisión: {} \n" +
                "║ Total Errores: {} \n" +
                "║ Total Cuentas Afectadas: {} \n" +
                "╚════════════════════════════════════════════════════════════╝",
                String.format("%.2f", totalIntereses),
                totalAplicados,
                totalRevisiones,
                totalErrores,
                interesesPorCuenta.size());
    }
}
