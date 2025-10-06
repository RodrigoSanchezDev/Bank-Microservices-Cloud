package com.duoc.bank.account.config;

import com.duoc.bank.account.model.User;
import com.duoc.bank.account.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Componente para inicializar datos por defecto en la base de datos
 */
@Component
public class DataInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        // Crear usuario admin por defecto si no existe
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@bank.com");
            admin.setEnabled(true);
            admin.setRoles(Set.of("ROLE_ADMIN", "ROLE_USER"));
            userRepository.save(admin);
            System.out.println("✅ Usuario admin creado exitosamente");
        }
    }
}
