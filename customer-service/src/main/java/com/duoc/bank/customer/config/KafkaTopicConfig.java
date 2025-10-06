package com.duoc.bank.customer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Configuración de topics de Kafka para Customer Service
 */
@Configuration
public class KafkaTopicConfig {
    
    public static final String CUSTOMER_CREATED_TOPIC = "customer-created-events";
    
    @Bean
    public NewTopic customerCreatedTopic() {
        return TopicBuilder.name(CUSTOMER_CREATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
