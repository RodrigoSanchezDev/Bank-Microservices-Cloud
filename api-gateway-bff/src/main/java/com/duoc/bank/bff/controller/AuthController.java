package com.duoc.bank.bff.controller;

import com.duoc.bank.bff.dto.AuthRequest;
import com.duoc.bank.bff.dto.AuthResponse;
import com.duoc.bank.bff.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private static final Map<String, UserInfo> USERS = new HashMap<>();

    static {
        USERS.put("admin", new UserInfo("admin123", List.of("ROLE_ADMIN", "ROLE_USER")));
        USERS.put("user", new UserInfo("user123", List.of("ROLE_USER")));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest request) {
        return Mono.fromCallable(() -> {
            UserInfo userInfo = USERS.get(request.username());
            
            if (userInfo == null || !userInfo.password.equals(request.password())) {
                return ResponseEntity.status(401).<AuthResponse>build();
            }
            
            String token = jwtTokenUtil.generateToken(request.username(), userInfo.roles);
            return ResponseEntity.ok(new AuthResponse(token, "Bearer", request.username()));
        });
    }

    private static class UserInfo {
        String password;
        List<String> roles;
        
        UserInfo(String password, List<String> roles) {
            this.password = password;
            this.roles = roles;
        }
    }
}
