package com.duoc.bank.bff.config;

import com.duoc.bank.bff.controller.BffController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * Configuración de rutas funcionales para los 3 BFF locales.
 * Usa programación funcional reactiva para registrar endpoints en WebFlux.
 * 
 * NOTA: Deshabilitado temporalmente - usando @RestController en BffController
 */
//@Configuration
public class LocalBffRoutesConfig {

    @Bean
    public RouterFunction<ServerResponse> bffFunctionalRoutes(BffController bffController) {
        return route()
            // ===== BFF WEB =====
            .GET("/api/web/health", request -> 
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(bffController.webHealth(), Object.class))
            .GET("/api/web/dashboard", request -> 
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(bffController.webDashboard(
                        request.queryParam("customerId").orElse("1")), Object.class))
            .GET("/api/web/analytics/spending", request -> 
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(bffController.webAnalytics(
                        request.queryParam("customerId").orElse("1"),
                        request.queryParam("days").orElse("30")), Object.class))
            .GET("/api/web/accounts/{id}", request -> 
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(bffController.webAccount(
                        Long.parseLong(request.pathVariable("id"))), Object.class))
            
            // ===== BFF MOBILE =====
            .GET("/api/mobile/health", request -> 
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(bffController.mobileHealth(), Object.class))
            .GET("/api/mobile/balance/{accountId}", request -> 
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(bffController.mobileBalance(
                        Long.parseLong(request.pathVariable("accountId"))), Object.class))
            .GET("/api/mobile/accounts", request -> 
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(bffController.mobileAccounts(), Object.class))
            .GET("/api/mobile/accounts/{id}", request -> 
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(bffController.mobileAccount(
                        Long.parseLong(request.pathVariable("id"))), Object.class))
            .GET("/api/mobile/transactions/recent", request -> 
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(bffController.mobileRecentTransactions(
                        Long.parseLong(request.queryParam("accountId").orElse("1"))), Object.class))
            .GET("/api/mobile/summary", request -> 
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(bffController.mobileSummary(
                        request.queryParam("customerId").orElse("1")), Object.class))
            .POST("/api/mobile/transfer", request -> 
                request.bodyToMono(java.util.Map.class)
                    .flatMap(body -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(bffController.mobileTransfer(body), Object.class)))
            .POST("/api/mobile/pay", request -> 
                request.bodyToMono(java.util.Map.class)
                    .flatMap(body -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(bffController.mobilePay(body), Object.class)))
            
            // ===== BFF ATM =====
            .GET("/api/atm/health", request -> 
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(bffController.atmHealth(), Object.class))
            .POST("/api/atm/balance", request -> 
                request.bodyToMono(java.util.Map.class)
                    .flatMap(body -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(bffController.atmBalance(body), Object.class)))
            .POST("/api/atm/withdraw", request -> 
                request.bodyToMono(java.util.Map.class)
                    .flatMap(body -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(bffController.atmWithdraw(body), Object.class)))
            .POST("/api/atm/deposit", request -> 
                request.bodyToMono(java.util.Map.class)
                    .flatMap(body -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(bffController.atmDeposit(body), Object.class)))
            .POST("/api/atm/mini-statement", request -> 
                request.bodyToMono(java.util.Map.class)
                    .flatMap(body -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(bffController.atmMiniStatement(body), Object.class)))
            .POST("/api/atm/change-pin", request -> 
                request.bodyToMono(java.util.Map.class)
                    .flatMap(body -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(bffController.atmChangePin(body), Object.class)))
            .POST("/api/atm/quick-transfer", request -> 
                request.bodyToMono(java.util.Map.class)
                    .flatMap(body -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(bffController.atmQuickTransfer(body), Object.class)))
            
            .build();
    }
}
