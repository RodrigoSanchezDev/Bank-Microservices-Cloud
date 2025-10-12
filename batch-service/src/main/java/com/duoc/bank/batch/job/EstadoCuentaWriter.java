package com.duoc.bank.batch.job;

import com.duoc.bank.batch.model.CuentaAnualCSV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Writer para guardar y generar resumen de estados de cuenta anuales
 */
@Component
public class EstadoCuentaWriter implements ItemWriter<CuentaAnualCSV> {

    private static final Logger logger = LoggerFactory.getLogger(EstadoCuentaWriter.class);
    
    private int totalCuentas = 0;
    private int totalAprobados = 0;
    private int totalRevisiones = 0;
    private int totalAnomalias = 0;
    private double sumaVariaciones = 0.0;
    private Map<String, Integer> cuentasPorTipo = new HashMap<>();

    @Override
    public void write(Chunk<? extends CuentaAnualCSV> chunk) throws Exception {
        for (CuentaAnualCSV cuenta : chunk) {
            logger.info("📄 Guardando estado de cuenta: {} - {} - Período: {} - Estado: {}", 
                cuenta.getCuentaId(), cuenta.getTitular(), cuenta.getPeriodo(), cuenta.getEstado());

            totalCuentas++;
            
            // Calcular variación
            double variacion = cuenta.getSaldoFinal() - cuenta.getSaldoInicial();
            sumaVariaciones += variacion;
            
            // Contar por tipo de cuenta
            cuentasPorTipo.merge(cuenta.getTipoCuenta(), 1, Integer::sum);
            
            // Contar por estado
            switch (cuenta.getEstado()) {
                case "APROBADO":
                    totalAprobados++;
                    break;
                case "REVISION":
                    totalRevisiones++;
                    break;
                case "ANOMALIA":
                    totalAnomalias++;
                    break;
            }

            logger.info("✅ Estado de cuenta guardado: {} - Saldo Inicial: {} - Saldo Final: {} - Variación: {}", 
                cuenta.getCuentaId(), cuenta.getSaldoInicial(), cuenta.getSaldoFinal(), 
                String.format("%.2f", variacion));
        }
        
        // Imprimir resumen parcial
        logger.info("📊 Resumen parcial - Total: {} - Aprobados: {} - Revisiones: {} - Anomalías: {}", 
            totalCuentas, totalAprobados, totalRevisiones, totalAnomalias);
    }

    public void imprimirResumen() {
        double promedioVariacion = totalCuentas > 0 ? sumaVariaciones / totalCuentas : 0;
        
        logger.info("\n" +
                "╔════════════════════════════════════════════════════════════╗\n" +
                "║      RESUMEN DE ESTADOS DE CUENTA ANUALES - AUDITORÍA     ║\n" +
                "╠════════════════════════════════════════════════════════════╣\n" +
                "║ Total Cuentas Procesadas: {} \n" +
                "║ Estados Aprobados: {} \n" +
                "║ Requieren Revisión: {} \n" +
                "║ Anomalías Detectadas: {} \n" +
                "║ Variación Promedio: ${} \n" +
                "║ Distribución por Tipo: {} \n" +
                "╚════════════════════════════════════════════════════════════╝",
                totalCuentas,
                totalAprobados,
                totalRevisiones,
                totalAnomalias,
                String.format("%.2f", promedioVariacion),
                cuentasPorTipo);
    }
}
