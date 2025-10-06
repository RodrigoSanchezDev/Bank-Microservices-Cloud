package com.duoc.bank.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Config Server - Servidor centralizado de configuración
 * 
 * Este servidor proporciona configuración centralizada para todos los microservicios.
 * Utiliza Spring Cloud Config para gestionar las propiedades de forma externa.
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
