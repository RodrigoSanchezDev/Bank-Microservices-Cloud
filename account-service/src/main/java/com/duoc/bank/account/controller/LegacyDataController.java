package com.duoc.bank.account.controller;

import com.duoc.bank.account.dto.CuentaAnualDTO;
import com.duoc.bank.account.dto.InteresDTO;
import com.duoc.bank.account.dto.TransaccionDTO;
import com.duoc.bank.account.service.LegacyDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller para exponer API de datos legacy migrados
 */
@Slf4j
@RestController
@RequestMapping("/api/legacy")
@Tag(name = "Legacy Data", description = "API para datos migrados del sistema legacy")
public class LegacyDataController {

    @Autowired
    private LegacyDataService legacyDataService;

    // ========== ENDPOINTS DE TRANSACCIONES ==========

    @GetMapping("/transacciones")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Obtener todas las transacciones", description = "Retorna todas las transacciones de todas las semanas con validación")
    public ResponseEntity<Map<String, Object>> obtenerTransacciones() {
        log.info("📊 Procesando todas las transacciones...");
        List<TransaccionDTO> transacciones = legacyDataService.procesarTransacciones();
        
        return ResponseEntity.ok(crearRespuesta(
            transacciones,
            transacciones.stream().filter(TransaccionDTO::isEsValido).collect(Collectors.toList()),
            transacciones.stream().filter(t -> !t.isEsValido()).collect(Collectors.toList())
        ));
    }

    @GetMapping("/transacciones/semana/{semana}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Obtener transacciones por semana", description = "Retorna transacciones de una semana específica (semana_1, semana_2, semana_3)")
    public ResponseEntity<Map<String, Object>> obtenerTransaccionesPorSemana(
            @Parameter(description = "Número de semana (semana_1, semana_2, semana_3)", required = true)
            @PathVariable String semana) {
        log.info("📊 Procesando transacciones de {}...", semana);
        List<TransaccionDTO> transacciones = legacyDataService.procesarTransaccionesPorSemana(semana);
        
        return ResponseEntity.ok(crearRespuesta(
            transacciones,
            transacciones.stream().filter(TransaccionDTO::isEsValido).collect(Collectors.toList()),
            transacciones.stream().filter(t -> !t.isEsValido()).collect(Collectors.toList())
        ));
    }

    @GetMapping("/transacciones/validas")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Obtener solo transacciones válidas", description = "Retorna únicamente las transacciones que pasaron la validación")
    public ResponseEntity<List<TransaccionDTO>> obtenerTransaccionesValidas() {
        log.info("✅ Obteniendo transacciones válidas...");
        List<TransaccionDTO> transacciones = legacyDataService.procesarTransacciones();
        List<TransaccionDTO> validas = transacciones.stream()
                .filter(TransaccionDTO::isEsValido)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(validas);
    }

    @GetMapping("/transacciones/invalidas")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener transacciones inválidas", description = "Retorna las transacciones que fallaron la validación (solo Admin)")
    public ResponseEntity<List<TransaccionDTO>> obtenerTransaccionesInvalidas() {
        log.info("❌ Obteniendo transacciones inválidas...");
        List<TransaccionDTO> transacciones = legacyDataService.procesarTransacciones();
        List<TransaccionDTO> invalidas = transacciones.stream()
                .filter(t -> !t.isEsValido())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(invalidas);
    }

    // ========== ENDPOINTS DE INTERESES ==========

    @GetMapping("/intereses")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Obtener todos los intereses", description = "Retorna todos los registros de intereses de todas las semanas")
    public ResponseEntity<Map<String, Object>> obtenerIntereses() {
        log.info("💰 Procesando todos los intereses...");
        List<InteresDTO> intereses = legacyDataService.procesarIntereses();
        
        return ResponseEntity.ok(crearRespuesta(
            intereses,
            intereses.stream().filter(InteresDTO::isEsValido).collect(Collectors.toList()),
            intereses.stream().filter(i -> !i.isEsValido()).collect(Collectors.toList())
        ));
    }

    @GetMapping("/intereses/semana/{semana}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Obtener intereses por semana", description = "Retorna intereses de una semana específica")
    public ResponseEntity<Map<String, Object>> obtenerInteresesPorSemana(
            @Parameter(description = "Número de semana (semana_1, semana_2, semana_3)", required = true)
            @PathVariable String semana) {
        log.info("💰 Procesando intereses de {}...", semana);
        List<InteresDTO> intereses = legacyDataService.procesarInteresesPorSemana(semana);
        
        return ResponseEntity.ok(crearRespuesta(
            intereses,
            intereses.stream().filter(InteresDTO::isEsValido).collect(Collectors.toList()),
            intereses.stream().filter(i -> !i.isEsValido()).collect(Collectors.toList())
        ));
    }

    @GetMapping("/intereses/validas")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Obtener solo intereses válidos", description = "Retorna únicamente los registros que pasaron la validación")
    public ResponseEntity<List<InteresDTO>> obtenerInteresesValidos() {
        log.info("✅ Obteniendo intereses válidos...");
        List<InteresDTO> intereses = legacyDataService.procesarIntereses();
        List<InteresDTO> validos = intereses.stream()
                .filter(InteresDTO::isEsValido)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(validos);
    }

    // ========== ENDPOINTS DE CUENTAS ANUALES ==========

    @GetMapping("/cuentas-anuales")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Obtener todas las cuentas anuales", description = "Retorna todo el historial de operaciones anuales")
    public ResponseEntity<Map<String, Object>> obtenerCuentasAnuales() {
        log.info("🏦 Procesando todas las cuentas anuales...");
        List<CuentaAnualDTO> cuentas = legacyDataService.procesarCuentasAnuales();
        
        return ResponseEntity.ok(crearRespuesta(
            cuentas,
            cuentas.stream().filter(CuentaAnualDTO::isEsValido).collect(Collectors.toList()),
            cuentas.stream().filter(c -> !c.isEsValido()).collect(Collectors.toList())
        ));
    }

    @GetMapping("/cuentas-anuales/semana/{semana}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Obtener cuentas anuales por semana", description = "Retorna cuentas anuales de una semana específica")
    public ResponseEntity<Map<String, Object>> obtenerCuentasAnualesPorSemana(
            @Parameter(description = "Número de semana (semana_1, semana_2, semana_3)", required = true)
            @PathVariable String semana) {
        log.info("🏦 Procesando cuentas anuales de {}...", semana);
        List<CuentaAnualDTO> cuentas = legacyDataService.procesarCuentasAnualesPorSemana(semana);
        
        return ResponseEntity.ok(crearRespuesta(
            cuentas,
            cuentas.stream().filter(CuentaAnualDTO::isEsValido).collect(Collectors.toList()),
            cuentas.stream().filter(c -> !c.isEsValido()).collect(Collectors.toList())
        ));
    }

    @GetMapping("/cuentas-anuales/validas")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Obtener solo cuentas anuales válidas", description = "Retorna únicamente las cuentas que pasaron la validación")
    public ResponseEntity<List<CuentaAnualDTO>> obtenerCuentasAnualesValidas() {
        log.info("✅ Obteniendo cuentas anuales válidas...");
        List<CuentaAnualDTO> cuentas = legacyDataService.procesarCuentasAnuales();
        List<CuentaAnualDTO> validas = cuentas.stream()
                .filter(CuentaAnualDTO::isEsValido)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(validas);
    }

    // ========== ENDPOINT DE RESUMEN ==========

    @GetMapping("/resumen")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener resumen de migración", description = "Retorna estadísticas generales de la migración (solo Admin)")
    public ResponseEntity<Map<String, Object>> obtenerResumen() {
        log.info("📈 Generando resumen de migración...");
        
        List<TransaccionDTO> transacciones = legacyDataService.procesarTransacciones();
        List<InteresDTO> intereses = legacyDataService.procesarIntereses();
        List<CuentaAnualDTO> cuentas = legacyDataService.procesarCuentasAnuales();
        
        Map<String, Object> resumen = new HashMap<>();
        resumen.put("transacciones", Map.of(
            "total", transacciones.size(),
            "validas", transacciones.stream().filter(TransaccionDTO::isEsValido).count(),
            "invalidas", transacciones.stream().filter(t -> !t.isEsValido()).count()
        ));
        resumen.put("intereses", Map.of(
            "total", intereses.size(),
            "validos", intereses.stream().filter(InteresDTO::isEsValido).count(),
            "invalidos", intereses.stream().filter(i -> !i.isEsValido()).count()
        ));
        resumen.put("cuentas_anuales", Map.of(
            "total", cuentas.size(),
            "validas", cuentas.stream().filter(CuentaAnualDTO::isEsValido).count(),
            "invalidas", cuentas.stream().filter(c -> !c.isEsValido()).count()
        ));
        
        long totalRegistros = transacciones.size() + intereses.size() + cuentas.size();
        long totalValidos = 
            transacciones.stream().filter(TransaccionDTO::isEsValido).count() +
            intereses.stream().filter(InteresDTO::isEsValido).count() +
            cuentas.stream().filter(CuentaAnualDTO::isEsValido).count();
        
        resumen.put("total_registros", totalRegistros);
        resumen.put("total_validos", totalValidos);
        resumen.put("total_invalidos", totalRegistros - totalValidos);
        resumen.put("tasa_exito", String.format("%.2f%%", (totalValidos * 100.0 / totalRegistros)));
        
        return ResponseEntity.ok(resumen);
    }

    // ========== Método auxiliar ==========

    private <T> Map<String, Object> crearRespuesta(List<T> todos, List<T> validos, List<T> invalidos) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("total", todos.size());
        respuesta.put("validos", validos.size());
        respuesta.put("invalidos", invalidos.size());
        respuesta.put("datos", todos);
        respuesta.put("tasa_exito", String.format("%.2f%%", (validos.size() * 100.0 / todos.size())));
        return respuesta;
    }
}
