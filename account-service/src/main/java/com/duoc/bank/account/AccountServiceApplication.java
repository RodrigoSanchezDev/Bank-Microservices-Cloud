package com.duoc.bank.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Account Service - Microservicio de Gestión de Cuentas Bancarias
 * 
 * Microservicio con patrones de resiliencia:
 * - Circuit Breaker
 * - Retry Pattern
 * - Rate Limiter
 * - Time Limiter
 * 
 * Seguridad:
 * - Autenticación JWT
 * - Spring Security
 */
@SpringBootApplication
@EnableDiscoveryClient
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
