package com.duoc.bank.bff.filter;

import com.duoc.bank.bff.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filtro JWT simplificado para validar tokens en el BFF
 * NO usa Spring Security, solo valida el token JWT manualmente
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    // Rutas públicas que NO requieren autenticación
    private static final String[] PUBLIC_PATHS = {
        "/api/auth/login",
        "/actuator/health",
        "/actuator/info",
        "/v3/api-docs",
        "/swagger-ui"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // Permitir rutas públicas sin autenticación
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // Extraer token del header Authorization
        String authHeader = request.getHeaders().getFirst("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7); // Remover "Bearer "

        // Validar token
        try {
            if (!jwtTokenUtil.validateToken(token)) {
                return onError(exchange, "Invalid JWT token", HttpStatus.UNAUTHORIZED);
            }

            // Extraer username del token y agregarlo como header para servicios downstream
            String username = jwtTokenUtil.extractUsername(token);
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Id", username)
                .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            return onError(exchange, "JWT validation error: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isPublicPath(String path) {
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String error, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json");
        
        String errorJson = String.format("{\"error\":\"%s\",\"status\":%d}", error, httpStatus.value());
        return response.writeWith(Mono.just(response.bufferFactory().wrap(errorJson.getBytes())));
    }

    @Override
    public int getOrder() {
        return -100; // Alta prioridad - ejecutar ANTES de otros filtros
    }
}
