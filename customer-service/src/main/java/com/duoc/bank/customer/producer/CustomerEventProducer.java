package com.duoc.bank.customer.producer;

import com.duoc.bank.customer.config.KafkaTopicConfig;
import com.duoc.bank.customer.event.CustomerCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Producer de Kafka para enviar eventos de clientes creados
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerEventProducer {
    
    private final KafkaTemplate<String, CustomerCreatedEvent> kafkaTemplate;
    
    /**
     * Publica un evento cuando se crea un nuevo cliente
     */
    public void publishCustomerCreated(CustomerCreatedEvent event) {
        try {
            log.info("📤 Publicando evento CustomerCreated: {}", event);
            
            kafkaTemplate.send(KafkaTopicConfig.CUSTOMER_CREATED_TOPIC, 
                             event.getCustomerId().toString(), 
                             event);
            
            log.info("✅ Evento CustomerCreated publicado exitosamente para customerId: {}", 
                    event.getCustomerId());
            
        } catch (Exception e) {
            log.error("❌ Error al publicar evento CustomerCreated: {}", e.getMessage(), e);
            throw new RuntimeException("Error al publicar evento a Kafka", e);
        }
    }
}
