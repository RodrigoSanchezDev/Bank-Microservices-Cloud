package com.duoc.bank.batch.config;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
    
    /**
     * Inicializa el schema de Spring Batch AUTOMÁTICAMENTE al iniciar la aplicación.
     * Usa CommandLineRunner con @Order(1) para ejecutarse PRIMERO, después de que
     * Spring Boot complete la configuración del DataSource.
     * 
     * IMPORTANTE para Spring Boot 3.x / Spring Batch 5.x:
     * - La propiedad initialize-schema: always YA NO FUNCIONA
     * - Debemos inicializar manualmente usando CommandLineRunner
     * - Esto garantiza 100% de automatización - sin pasos manuales
     */
    @Bean
    @Order(1)
    public CommandLineRunner initBatchSchema(DataSource dataSource) {
        return args -> {
            System.out.println("==============================================");
            System.out.println("CommandLineRunner initBatchSchema EJECUTÁNDOSE");
            System.out.println("==============================================");
            try {
                // Verificar si las tablas ya existen
                try (var connection = dataSource.getConnection();
                     var stmt = connection.createStatement();
                     var rs = stmt.executeQuery("SELECT COUNT(*) FROM BATCH_JOB_INSTANCE WHERE 1=0")) {
                    System.out.println("✅ Spring Batch schema ya existe - no se requiere inicialización");
                    return;
                } catch (Exception checkException) {
                    // Las tablas no existen, proceder con la creación
                    System.out.println("🔄 Tablas no existen - Iniciando creación de schema Spring Batch...");
                }
                
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScript(new ClassPathResource("schema-batch.sql"));
                populator.setSeparator(";");
                populator.setContinueOnError(false);
                populator.execute(dataSource);
                
                System.out.println("✅ Spring Batch schema inicializado correctamente");
            } catch (Exception e) {
                System.err.println("❌ Error al inicializar Spring Batch schema: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("No se pudo inicializar el schema de Spring Batch", e);
            }
        };
    }
}
