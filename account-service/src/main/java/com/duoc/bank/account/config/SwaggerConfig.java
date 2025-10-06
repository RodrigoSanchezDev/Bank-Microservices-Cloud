package com.duoc.bank.account.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de Swagger/OpenAPI con autenticación JWT
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Bank Microservices - Legacy Data API")
                        .version("1.0.0")
                        .description("""
                                API REST para procesar datos legacy del sistema bancario.
                                
                                ## Autenticación
                                
                                Para usar esta API, primero debes autenticarte:
                                
                                1. **Endpoint de Login:** `POST /api/auth/login`
                                2. **Credenciales:**
                                   - **Username:** `admin`
                                   - **Password:** `admin123`
                                3. **Response:** Recibirás un token JWT
                                4. **Uso:** Haz clic en el botón "Authorize" 🔒 arriba y pega el token
                                
                                ## Endpoints Principales
                                
                                - **Transacciones:** Procesa transacciones diarias de las 3 semanas
                                - **Intereses:** Gestiona cálculos de intereses mensuales
                                - **Cuentas Anuales:** Historial de operaciones anuales
                                - **Resumen:** Estadísticas generales (solo Admin)
                                
                                ## Validaciones
                                
                                La API detecta y reporta:
                                - ✅ Montos negativos o cero
                                - ✅ Formatos de fecha inconsistentes
                                - ✅ Datos faltantes o nulos
                                - ✅ Registros duplicados
                                - ✅ Valores fuera de rango
                                """)
                        .contact(new Contact()
                                .name("DUOC UC - Desarrollo Backend III")
                                .email("soporte@duoc.cl"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Servidor Local"),
                        new Server()
                                .url("http://localhost:8081")
                                .description("Servidor Docker")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("""
                                        Ingresa el token JWT obtenido del endpoint `/api/auth/login`
                                        
                                        **Credenciales para login:**
                                        - Username: `admin`
                                        - Password: `admin123`
                                        
                                        No necesitas agregar 'Bearer' antes del token, solo pega el token directamente.
                                        """)));
    }
}
