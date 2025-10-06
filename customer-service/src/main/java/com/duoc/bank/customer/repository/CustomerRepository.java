package com.duoc.bank.customer.repository;

import com.duoc.bank.customer.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Customer
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByRut(String rut);
    
    Optional<Customer> findByEmail(String email);
    
    boolean existsByRut(String rut);
    
    boolean existsByEmail(String email);
}
