package com.duoc.bank.customer.controller;

import com.duoc.bank.customer.model.Customer;
import com.duoc.bank.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de clientes
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Obtiene todos los clientes
     */
    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        log.info("GET /api/customers - Obteniendo todos los clientes");
        List<Customer> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    /**
     * Obtiene un cliente por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        log.info("GET /api/customers/{} - Obteniendo cliente", id);
        return customerService.getCustomerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene un cliente por RUT
     */
    @GetMapping("/rut/{rut}")
    public ResponseEntity<Customer> getCustomerByRut(@PathVariable String rut) {
        log.info("GET /api/customers/rut/{} - Obteniendo cliente", rut);
        return customerService.getCustomerByRut(rut)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene un cliente por email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<Customer> getCustomerByEmail(@PathVariable String email) {
        log.info("GET /api/customers/email/{} - Obteniendo cliente", email);
        return customerService.getCustomerByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo cliente
     */
    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        log.info("POST /api/customers - Creando nuevo cliente");
        try {
            Customer createdCustomer = customerService.createCustomer(customer);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
        } catch (IllegalArgumentException e) {
            log.error("Error al crear cliente: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Actualiza un cliente existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        log.info("PUT /api/customers/{} - Actualizando cliente", id);
        try {
            Customer updatedCustomer = customerService.updateCustomer(id, customer);
            return ResponseEntity.ok(updatedCustomer);
        } catch (IllegalArgumentException e) {
            log.error("Error al actualizar cliente: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina un cliente
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        log.info("DELETE /api/customers/{} - Eliminando cliente", id);
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Error al eliminar cliente: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Customer Service is running");
    }
}
