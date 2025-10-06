package com.duoc.bank.account.service;

import com.duoc.bank.account.dto.CuentaAnualDTO;
import com.duoc.bank.account.dto.InteresDTO;
import com.duoc.bank.account.dto.TransaccionDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para procesar datos legacy del sistema bancario
 */
@Slf4j
@Service
public class LegacyDataService {

    private static final String[] SEMANAS = {"semana_1", "semana_2", "semana_3"};
    private static final DateTimeFormatter[] FORMATTERS = {
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd")
    };

    /**
     * Procesa todas las transacciones de todas las semanas
     */
    @CircuitBreaker(name = "legacyData", fallbackMethod = "fallbackTransacciones")
    @Retry(name = "legacyData")
    public List<TransaccionDTO> procesarTransacciones() {
        List<TransaccionDTO> todasTransacciones = new ArrayList<>();
        
        for (String semana : SEMANAS) {
            String path = "data/" + semana + "/transacciones.csv";
            try {
                List<TransaccionDTO> transacciones = leerTransacciones(path, semana);
                todasTransacciones.addAll(transacciones);
                log.info("✅ Procesadas {} transacciones de {}", transacciones.size(), semana);
            } catch (Exception e) {
                log.error("❌ Error procesando transacciones de {}: {}", semana, e.getMessage());
            }
        }
        
        return todasTransacciones;
    }

    /**
     * Procesa todos los intereses de todas las semanas
     */
    @CircuitBreaker(name = "legacyData", fallbackMethod = "fallbackIntereses")
    @Retry(name = "legacyData")
    public List<InteresDTO> procesarIntereses() {
        List<InteresDTO> todosIntereses = new ArrayList<>();
        
        for (String semana : SEMANAS) {
            String path = "data/" + semana + "/intereses.csv";
            try {
                List<InteresDTO> intereses = leerIntereses(path, semana);
                todosIntereses.addAll(intereses);
                log.info("✅ Procesados {} intereses de {}", intereses.size(), semana);
            } catch (Exception e) {
                log.error("❌ Error procesando intereses de {}: {}", semana, e.getMessage());
            }
        }
        
        return todosIntereses;
    }

    /**
     * Procesa todas las cuentas anuales de todas las semanas
     */
    @CircuitBreaker(name = "legacyData", fallbackMethod = "fallbackCuentasAnuales")
    @Retry(name = "legacyData")
    public List<CuentaAnualDTO> procesarCuentasAnuales() {
        List<CuentaAnualDTO> todasCuentas = new ArrayList<>();
        
        for (String semana : SEMANAS) {
            String path = "data/" + semana + "/cuentas_anuales.csv";
            try {
                List<CuentaAnualDTO> cuentas = leerCuentasAnuales(path, semana);
                todasCuentas.addAll(cuentas);
                log.info("✅ Procesadas {} cuentas anuales de {}", cuentas.size(), semana);
            } catch (Exception e) {
                log.error("❌ Error procesando cuentas anuales de {}: {}", semana, e.getMessage());
            }
        }
        
        return todasCuentas;
    }

    /**
     * Procesa transacciones de una semana específica
     */
    public List<TransaccionDTO> procesarTransaccionesPorSemana(String semana) {
        String path = "data/" + semana + "/transacciones.csv";
        try {
            return leerTransacciones(path, semana);
        } catch (IOException e) {
            log.error("Error leyendo transacciones de {}: {}", semana, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Procesa intereses de una semana específica
     */
    public List<InteresDTO> procesarInteresesPorSemana(String semana) {
        String path = "data/" + semana + "/intereses.csv";
        try {
            return leerIntereses(path, semana);
        } catch (IOException e) {
            log.error("Error leyendo intereses de {}: {}", semana, e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Procesa cuentas anuales de una semana específica
     */
    public List<CuentaAnualDTO> procesarCuentasAnualesPorSemana(String semana) {
        String path = "data/" + semana + "/cuentas_anuales.csv";
        try {
            return leerCuentasAnuales(path, semana);
        } catch (IOException e) {
            log.error("Error leyendo cuentas anuales de {}: {}", semana, e.getMessage());
            return new ArrayList<>();
        }
    }

    // ========== Métodos privados de lectura ==========

    private List<TransaccionDTO> leerTransacciones(String path, String semana) throws IOException {
        List<TransaccionDTO> transacciones = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(path);
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String linea;
            boolean primeraLinea = true;
            
            while ((linea = reader.readLine()) != null) {
                if (primeraLinea) {
                    primeraLinea = false;
                    continue; // Saltar encabezado
                }
                
                TransaccionDTO dto = parsearTransaccion(linea, semana);
                transacciones.add(dto);
            }
        }
        
        return transacciones;
    }

    private TransaccionDTO parsearTransaccion(String linea, String semana) {
        String[] campos = linea.split(",");
        TransaccionDTO dto = new TransaccionDTO();
        dto.setSemana(semana);
        dto.setEsValido(true);
        
        try {
            dto.setId(Long.parseLong(campos[0].trim()));
            dto.setFecha(parsearFecha(campos[1].trim()));
            
            BigDecimal monto = new BigDecimal(campos[2].trim());
            dto.setMonto(monto);
            
            // Validar monto
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                dto.setEsValido(false);
                dto.setMotivoInvalidez("Monto negativo o cero");
            }
            
            dto.setTipo(campos[3].trim());
            
        } catch (Exception e) {
            dto.setEsValido(false);
            dto.setMotivoInvalidez("Error parseando datos: " + e.getMessage());
        }
        
        return dto;
    }

    private List<InteresDTO> leerIntereses(String path, String semana) throws IOException {
        List<InteresDTO> intereses = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(path);
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String linea;
            boolean primeraLinea = true;
            
            while ((linea = reader.readLine()) != null) {
                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }
                
                InteresDTO dto = parsearInteres(linea, semana);
                intereses.add(dto);
            }
        }
        
        return intereses;
    }

    private InteresDTO parsearInteres(String linea, String semana) {
        String[] campos = linea.split(",");
        InteresDTO dto = new InteresDTO();
        dto.setSemana(semana);
        dto.setEsValido(true);
        
        try {
            dto.setCuentaId(Long.parseLong(campos[0].trim()));
            dto.setNombre(campos[1].trim());
            
            String saldoStr = campos[2].trim();
            if (saldoStr.isEmpty()) {
                dto.setEsValido(false);
                dto.setMotivoInvalidez("Saldo vacío");
                dto.setSaldo(BigDecimal.ZERO);
            } else {
                dto.setSaldo(new BigDecimal(saldoStr));
            }
            
            Integer edad = Integer.parseInt(campos[3].trim());
            dto.setEdad(edad);
            
            // Validar edad realista
            if (edad < 18 || edad > 120) {
                dto.setEsValido(false);
                dto.setMotivoInvalidez("Edad no válida: " + edad);
            }
            
            dto.setTipo(campos[4].trim());
            
        } catch (Exception e) {
            dto.setEsValido(false);
            dto.setMotivoInvalidez("Error parseando datos: " + e.getMessage());
        }
        
        return dto;
    }

    private List<CuentaAnualDTO> leerCuentasAnuales(String path, String semana) throws IOException {
        List<CuentaAnualDTO> cuentas = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(path);
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String linea;
            boolean primeraLinea = true;
            
            while ((linea = reader.readLine()) != null) {
                if (primeraLinea) {
                    primeraLinea = false;
                    continue;
                }
                
                CuentaAnualDTO dto = parsearCuentaAnual(linea, semana);
                cuentas.add(dto);
            }
        }
        
        return cuentas;
    }

    private CuentaAnualDTO parsearCuentaAnual(String linea, String semana) {
        String[] campos = linea.split(",");
        CuentaAnualDTO dto = new CuentaAnualDTO();
        dto.setSemana(semana);
        dto.setEsValido(true);
        
        try {
            dto.setCuentaId(Long.parseLong(campos[0].trim()));
            dto.setFecha(parsearFecha(campos[1].trim()));
            dto.setTransaccion(campos[2].trim());
            
            BigDecimal monto = new BigDecimal(campos[3].trim());
            dto.setMonto(monto);
            
            // Validar monto negativo en operaciones de depósito
            if (dto.getTransaccion().equals("deposito") && monto.compareTo(BigDecimal.ZERO) <= 0) {
                dto.setEsValido(false);
                dto.setMotivoInvalidez("Monto de depósito negativo o cero");
            }
            
            String descripcion = campos.length > 4 ? campos[4].trim() : "";
            if (descripcion.isEmpty()) {
                dto.setEsValido(false);
                dto.setMotivoInvalidez("Descripción faltante");
            }
            dto.setDescripcion(descripcion);
            
        } catch (Exception e) {
            dto.setEsValido(false);
            dto.setMotivoInvalidez("Error parseando datos: " + e.getMessage());
        }
        
        return dto;
    }

    private LocalDate parsearFecha(String fechaStr) {
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return LocalDate.parse(fechaStr, formatter);
            } catch (DateTimeParseException e) {
                // Intentar siguiente formato
            }
        }
        throw new DateTimeParseException("Formato de fecha no soportado", fechaStr, 0);
    }

    // ========== Fallback methods ==========

    private List<TransaccionDTO> fallbackTransacciones(Exception e) {
        log.error("Circuit breaker activado para transacciones: {}", e.getMessage());
        return new ArrayList<>();
    }

    private List<InteresDTO> fallbackIntereses(Exception e) {
        log.error("Circuit breaker activado para intereses: {}", e.getMessage());
        return new ArrayList<>();
    }

    private List<CuentaAnualDTO> fallbackCuentasAnuales(Exception e) {
        log.error("Circuit breaker activado para cuentas anuales: {}", e.getMessage());
        return new ArrayList<>();
    }
}
