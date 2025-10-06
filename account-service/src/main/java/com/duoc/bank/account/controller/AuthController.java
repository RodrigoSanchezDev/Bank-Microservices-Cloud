package com.duoc.bank.account.controller;

import com.duoc.bank.account.dto.AuthRequest;
import com.duoc.bank.account.dto.AuthResponse;
import com.duoc.bank.account.security.JwtTokenUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

/**
 * Controller para autenticación JWT
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API de autenticación y autorización")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autenticar usuario y obtener token JWT")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            log.info("🔐 Intento de login para usuario: {}", authRequest.getUsername());
            
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    authRequest.getUsername(),
                    authRequest.getPassword()
                )
            );

            final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
            final String token = jwtTokenUtil.generateToken(userDetails);

            log.info("✅ Login exitoso para usuario: {}", authRequest.getUsername());
            
            return ResponseEntity.ok(new AuthResponse(token));
            
        } catch (BadCredentialsException e) {
            log.error("❌ Credenciales inválidas para usuario: {}", authRequest.getUsername());
            return ResponseEntity.status(401).body("Credenciales inválidas");
        }
    }
}
