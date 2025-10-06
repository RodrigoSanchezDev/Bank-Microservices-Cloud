package com.duoc.bank.customer.service;

import com.duoc.bank.customer.model.Customer;
import com.duoc.bank.customer.repository.CustomerRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de clientes con patrones de resiliencia
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Obtiene todos los clientes
     */
    @CircuitBreaker(name = "customerService", fallbackMethod = "getAllCustomersFallback")
    @Retry(name = "customerService")
    public List<Customer> getAllCustomers() {
        log.info("Obteniendo todos los clientes");
        return customerRepository.findAll();
    }

    /**
     * Obtiene un cliente por ID
     */
    @CircuitBreaker(name = "customerService", fallbackMethod = "getCustomerByIdFallback")
    @Retry(name = "customerService")
    public Optional<Customer> getCustomerById(Long id) {
        log.info("Obteniendo cliente con ID: {}", id);
        return customerRepository.findById(id);
    }

    /**
     * Obtiene un cliente por RUT
     */
    @CircuitBreaker(name = "customerService", fallbackMethod = "getCustomerByRutFallback")
    @Retry(name = "customerService")
    public Optional<Customer> getCustomerByRut(String rut) {
        log.info("Obteniendo cliente con RUT: {}", rut);
        return customerRepository.findByRut(rut);
    }

    /**
     * Obtiene un cliente por email
     */
    @CircuitBreaker(name = "customerService", fallbackMethod = "getCustomerByEmailFallback")
    @Retry(name = "customerService")
    public Optional<Customer> getCustomerByEmail(String email) {
        log.info("Obteniendo cliente con email: {}", email);
        return customerRepository.findByEmail(email);
    }

    /**
     * Crea un nuevo cliente
     */
    @Transactional
    @CircuitBreaker(name = "customerService", fallbackMethod = "createCustomerFallback")
    @Retry(name = "customerService")
    public Customer createCustomer(Customer customer) {
        log.info("Creando nuevo cliente con RUT: {}", customer.getRut());
        
        if (customerRepository.existsByRut(customer.getRut())) {
            throw new IllegalArgumentException("Ya existe un cliente con el RUT: " + customer.getRut());
        }
        
        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new IllegalArgumentException("Ya existe un cliente con el email: " + customer.getEmail());
        }
        
        return customerRepository.save(customer);
    }

    /**
     * Actualiza un cliente existente
     */
    @Transactional
    @CircuitBreaker(name = "customerService", fallbackMethod = "updateCustomerFallback")
    @Retry(name = "customerService")
    public Customer updateCustomer(Long id, Customer customerDetails) {
        log.info("Actualizando cliente con ID: {}", id);
        
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));
        
        customer.setFirstName(customerDetails.getFirstName());
        customer.setLastName(customerDetails.getLastName());
        customer.setEmail(customerDetails.getEmail());
        customer.setPhone(customerDetails.getPhone());
        customer.setAddress(customerDetails.getAddress());
        customer.setBirthDate(customerDetails.getBirthDate());
        customer.setStatus(customerDetails.getStatus());
        
        return customerRepository.save(customer);
    }

    /**
     * Elimina un cliente
     */
    @Transactional
    @CircuitBreaker(name = "customerService", fallbackMethod = "deleteCustomerFallback")
    @Retry(name = "customerService")
    public void deleteCustomer(Long id) {
        log.info("Eliminando cliente con ID: {}", id);
        
        if (!customerRepository.existsById(id)) {
            throw new IllegalArgumentException("Cliente no encontrado con ID: " + id);
        }
        
        customerRepository.deleteById(id);
    }

    // Fallback methods

    private List<Customer> getAllCustomersFallback(Exception e) {
        log.error("Error al obtener clientes, retornando lista vacía. Error: {}", e.getMessage());
        return List.of();
    }

    private Optional<Customer> getCustomerByIdFallback(Long id, Exception e) {
        log.error("Error al obtener cliente con ID: {}. Error: {}", id, e.getMessage());
        return Optional.empty();
    }

    private Optional<Customer> getCustomerByRutFallback(String rut, Exception e) {
        log.error("Error al obtener cliente con RUT: {}. Error: {}", rut, e.getMessage());
        return Optional.empty();
    }

    private Optional<Customer> getCustomerByEmailFallback(String email, Exception e) {
        log.error("Error al obtener cliente con email: {}. Error: {}", email, e.getMessage());
        return Optional.empty();
    }

    private Customer createCustomerFallback(Customer customer, Exception e) {
        log.error("Error al crear cliente. Error: {}", e.getMessage());
        throw new RuntimeException("No se pudo crear el cliente en este momento. Por favor, intente más tarde.", e);
    }

    private Customer updateCustomerFallback(Long id, Customer customer, Exception e) {
        log.error("Error al actualizar cliente con ID: {}. Error: {}", id, e.getMessage());
        throw new RuntimeException("No se pudo actualizar el cliente en este momento. Por favor, intente más tarde.", e);
    }

    private void deleteCustomerFallback(Long id, Exception e) {
        log.error("Error al eliminar cliente con ID: {}. Error: {}", id, e.getMessage());
        throw new RuntimeException("No se pudo eliminar el cliente en este momento. Por favor, intente más tarde.", e);
    }
}
