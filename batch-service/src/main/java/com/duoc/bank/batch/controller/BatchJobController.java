package com.duoc.bank.batch.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para ejecutar Jobs de Spring Batch manualmente
 * Todos los endpoints están protegidos por JWT a través del API Gateway BFF
 * Acceso: https://localhost:8443/api/batch/**
 */
@RestController
@RequestMapping("/api/batch")
public class BatchJobController {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job transaccionesDiariasJob;

    @Autowired
    private Job interesesMensualesJob;

    @Autowired
    private Job estadosCuentaAnualesJob;

    @PostMapping("/jobs/transacciones")
    public ResponseEntity<Map<String, String>> runTransaccionesJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            
            jobLauncher.run(transaccionesDiariasJob, params);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("job", "transaccionesDiariasJob");
            response.put("message", "Job ejecutado exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("job", "transaccionesDiariasJob");
            error.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/jobs/intereses")
    public ResponseEntity<Map<String, String>> runInteresesJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            
            jobLauncher.run(interesesMensualesJob, params);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("job", "interesesMensualesJob");
            response.put("message", "Job ejecutado exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("job", "interesesMensualesJob");
            error.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/jobs/estados-cuenta")
    public ResponseEntity<Map<String, String>> runEstadosCuentaJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            
            jobLauncher.run(estadosCuentaAnualesJob, params);
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("job", "estadosCuentaAnualesJob");
            response.put("message", "Job ejecutado exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("status", "ERROR");
            error.put("job", "estadosCuentaAnualesJob");
            error.put("message", e.getMessage());
            
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/jobs/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("batch-service", "UP");
        status.put("jobs-available", "3");
        status.put("jobs", "transaccionesDiariasJob, interesesMensualesJob, estadosCuentaAnualesJob");
        
        return ResponseEntity.ok(status);
    }
}
