package com.duoc.bank.transaction.consumer;

import com.duoc.bank.transaction.event.CustomerCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumer de Kafka para procesar eventos de clientes creados
 */
@Slf4j
@Component
public class CustomerEventConsumer {
    
    /**
     * Escucha y procesa eventos de clientes creados
     */
    @KafkaListener(
        topics = "customer-created-events",
        groupId = "transaction-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeCustomerCreated(CustomerCreatedEvent event) {
        try {
            log.info("📥 Evento CustomerCreated recibido: {}", event);
            
            // Aquí puedes procesar el evento según la lógica de negocio
            // Por ejemplo: crear una cuenta inicial, registrar auditoría, etc.
            
            log.info("✅ Cliente procesado exitosamente - ID: {}, Nombre: {} {}", 
                    event.getCustomerId(), 
                    event.getFirstName(), 
                    event.getLastName());
            
            // Simulación de lógica de negocio
            processNewCustomer(event);
            
        } catch (Exception e) {
            log.error("❌ Error al procesar evento CustomerCreated: {}", e.getMessage(), e);
            // Aquí podrías implementar lógica de reintento o dead letter queue
        }
    }
    
    /**
     * Procesa la lógica de negocio para un nuevo cliente
     */
    private void processNewCustomer(CustomerCreatedEvent event) {
        log.info("🔄 Procesando nuevo cliente en Transaction Service...");
        log.info("   - Customer ID: {}", event.getCustomerId());
        log.info("   - RUT: {}", event.getRut());
        log.info("   - Email: {}", event.getEmail());
        log.info("   - Status: {}", event.getStatus());
        log.info("   - Created At: {}", event.getCreatedAt());
        
        // Aquí puedes agregar lógica adicional como:
        // - Crear cuenta bancaria inicial
        // - Enviar email de bienvenida
        // - Registrar en sistema de auditoría
        // - Configurar límites de transacciones
    }
}
