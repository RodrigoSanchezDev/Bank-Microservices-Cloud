package com.duoc.bank.bff;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API Gateway BFF - Backend for Frontend
 * 
 * Punto de entrada único para todas las peticiones del cliente.
 * Características:
 * - Autenticación JWT centralizada
 * - HTTPS habilitado
 * - Enrutamiento a microservicios
 * - Circuit Breaker y Retry patterns
 * - Rate Limiting
 */
@SpringBootApplication
@EnableDiscoveryClient
public class BffGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(BffGatewayApplication.class, args);
    }
}
