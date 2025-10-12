package com.duoc.bank.bff.controller;

import com.duoc.bank.bff.dto.MobileAccountResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler BFF con 3 canales especializados:
 * - Web: Datos completos para navegadores  
 * - Mobile: Datos ligeros para apps móviles
 * - ATM: Operaciones seguras para cajeros automáticos
 */
@RestController
public class BffController {

    // ============================================
    // 🌐 BFF WEB (Navegadores)
    // ============================================

    @GetMapping("/api/web/health")
    public Mono<Map<String, Object>> webHealth() {
        return Mono.just(Map.of(
            "bff", "web",
            "status", "UP",
            "timestamp", LocalDateTime.now().toString(),
            "features", "full-data,analytics,complex-queries"
        ));
    }

    @GetMapping("/api/web/dashboard")
    public Mono<Map<String, Object>> webDashboard(@RequestParam(defaultValue = "1") String customerId) {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("customer", Map.of(
            "id", customerId,
            "name", "Cliente Web",
            "segment", "PREMIUM",
            "lastLogin", LocalDateTime.now()
        ));
        dashboard.put("accounts", List.of(
            Map.of("accountNumber", "0000001", "balance", 1500000.0, "type", "CHECKING"),
            Map.of("accountNumber", "0000002", "balance", 500000.0, "type", "SAVINGS")
        ));
        dashboard.put("recentTransactions", List.of(
            Map.of("date", LocalDateTime.now(), "description", "Compra Supermercado", "amount", -45000.0),
            Map.of("date", LocalDateTime.now(), "description", "Transferencia recibida", "amount", 200000.0)
        ));
        dashboard.put("creditCards", List.of(
            Map.of("last4", "4532", "limit", 3000000.0, "available", 2500000.0)
        ));
        dashboard.put("analytics", Map.of(
            "monthlyExpenses", 850000.0,
            "monthlyIncome", 1200000.0,
            "categoryBreakdown", Map.of(
                "Alimentación", 350000.0,
                "Transporte", 150000.0,
                "Entretenimiento", 100000.0
            )
        ));
        return Mono.just(dashboard);
    }

    @GetMapping("/api/web/analytics/spending")
    public Mono<Map<String, Object>> webAnalytics(
            @RequestParam(defaultValue = "1") String customerId,
            @RequestParam(defaultValue = "30") String days) {
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("period", days + " días");
        analytics.put("totalSpent", 1850000.0);
        analytics.put("totalIncome", 2500000.0);
        analytics.put("netSavings", 650000.0);
        analytics.put("categoryBreakdown", Map.of(
            "Alimentación", Map.of("amount", 450000.0, "percentage", 24.3, "trend", "up"),
            "Transporte", Map.of("amount", 300000.0, "percentage", 16.2, "trend", "stable"),
            "Servicios", Map.of("amount", 250000.0, "percentage", 13.5, "trend", "down"),
            "Entretenimiento", Map.of("amount", 200000.0, "percentage", 10.8, "trend", "up"),
            "Otros", Map.of("amount", 650000.0, "percentage", 35.2, "trend", "stable")
        ));
        analytics.put("dailyAverage", 61666.67);
        analytics.put("projectedEndOfMonth", 1850000.0 * (30.0 / Double.parseDouble(days)));
        analytics.put("recommendations", List.of(
            "Gastos en Alimentación 15% sobre promedio",
            "Buen control en Servicios",
            "Considera reducir Entretenimiento"
        ));
        return Mono.just(analytics);
    }

    @GetMapping("/api/web/accounts/{id}")
    public Mono<Map<String, Object>> webAccount(@PathVariable Long id) {
        Map<String, Object> account = new HashMap<>();
        account.put("id", id);
        account.put("accountNumber", "0000001");
        account.put("customerId", 1L);
        account.put("customerName", "Juan Pérez");
        account.put("customerEmail", "juan.perez@example.com");
        account.put("customerPhone", "+56912345678");
        account.put("balance", 1500000.0);
        account.put("availableBalance", 1450000.0);
        account.put("type", "CHECKING");
        account.put("status", "ACTIVE");
        account.put("currency", "CLP");
        return Mono.just(account);
    }

    // ============================================
    // 📱 BFF MOBILE (iOS/Android)
    // ============================================

    @GetMapping("/api/mobile/health")
    public Mono<Map<String, Object>> mobileHealth() {
        return Mono.just(Map.of(
            "bff", "mobile",
            "ok", true,
            "v", "1.0",
            "size", "compact"
        ));
    }

    @GetMapping("/api/mobile/balance/{accountId}")
    public Mono<Map<String, Object>> mobileBalance(@PathVariable Long accountId) {
        return Mono.just(Map.of(
            "bal", 1500000.0,
            "cur", "CLP"
        ));
    }

    @GetMapping("/api/mobile/accounts")
    public Mono<List<MobileAccountResponse>> mobileAccounts() {
        return Mono.just(List.of(
            new MobileAccountResponse(1L, "****0001", 1500000.0, "ACTIVE"),
            new MobileAccountResponse(2L, "****0002", 500000.0, "ACTIVE")
        ));
    }

    @GetMapping("/api/mobile/accounts/{id}")
    public Mono<MobileAccountResponse> mobileAccount(@PathVariable Long id) {
        return Mono.just(new MobileAccountResponse(id, "****0001", 1500000.0, "ACTIVE"));
    }

    @GetMapping("/api/mobile/transactions/recent")
    public Mono<List<Map<String, Object>>> mobileRecentTransactions(@RequestParam(defaultValue = "1") Long accountId) {
        return Mono.just(List.of(
            Map.of("id", 1, "desc", "Supermercado", "amt", -45000.0, "date", "07-10-2025"),
            Map.of("id", 2, "desc", "Transferencia", "amt", 200000.0, "date", "06-10-2025"),
            Map.of("id", 3, "desc", "Pago servicio", "amt", -89000.0, "date", "05-10-2025")
        ));
    }

    @GetMapping("/api/mobile/summary")
    public Mono<Map<String, Object>> mobileSummary(@RequestParam(defaultValue = "1") String customerId) {
        return Mono.just(Map.of(
            "bal", 2000000.0,
            "in", 200000.0,
            "out", -134000.0,
            "txs", 5,
            "alerts", 2
        ));
    }

    @PostMapping("/api/mobile/transfer")
    public Mono<Map<String, Object>> mobileTransfer(@RequestBody Map<String, Object> request) {
        return Mono.just(Map.of(
            "ok", true,
            "txId", "TXN-" + System.currentTimeMillis(),
            "amt", request.get("amount"),
            "to", request.get("destinationAccount")
        ));
    }

    @PostMapping("/api/mobile/pay")
    public Mono<Map<String, Object>> mobilePay(@RequestBody Map<String, Object> request) {
        return Mono.just(Map.of(
            "ok", true,
            "txId", "PAY-" + System.currentTimeMillis(),
            "amt", request.get("amount"),
            "service", request.get("service")
        ));
    }

    // ============================================
    // 🏧 BFF ATM (Cajeros Automáticos)
    // ============================================

    @GetMapping("/api/atm/health")
    public Mono<Map<String, Object>> atmHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("bff", "atm");
        health.put("atmId", "ATM-SCL-001");
        health.put("status", "OPERATIONAL");
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("capabilities", List.of(
            "BALANCE_INQUIRY",
            "WITHDRAWAL",
            "DEPOSIT",
            "QUICK_TRANSFER",
            "PIN_CHANGE",
            "MINI_STATEMENT"
        ));
        health.put("security", Map.of(
            "encryption", "AES-256",
            "authentication", "PIN + EMV",
            "audit", "ENABLED"
        ));
        return Mono.just(health);
    }

    @PostMapping("/api/atm/balance")
    public Mono<Map<String, Object>> atmBalance(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("operationType", "BALANCE_INQUIRY");
        response.put("amount", 0.0);
        response.put("newBalance", 1500000.0);
        response.put("receiptId", "RCP-" + System.currentTimeMillis());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("atmId", "ATM-SCL-001");
        response.put("security", Map.of(
            "transactionId", "TXN-" + System.currentTimeMillis(),
            "authorizationCode", "AUTH-123456",
            "encryptedCardData", "**** **** **** 9012"
        ));
        return Mono.just(response);
    }

    @PostMapping("/api/atm/withdraw")
    public Mono<Map<String, Object>> atmWithdraw(@RequestBody Map<String, Object> request) {
        Number amountNum = (Number) request.get("amount");
        Double amount = amountNum != null ? amountNum.doubleValue() : 0.0;

        // Validaciones ATM
        if (amount <= 0 || amount > 400000) {
            return Mono.just(Map.of(
                "success", false,
                "operationType", "WITHDRAWAL",
                "amount", amount,
                "error", "Amount must be between $1 and $400,000"
            ));
        }

        if (amount % 10000 != 0) {
            return Mono.just(Map.of(
                "success", false,
                "operationType", "WITHDRAWAL",
                "amount", amount,
                "error", "Amount must be multiple of $10,000"
            ));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("operationType", "WITHDRAWAL");
        response.put("amount", amount);
        response.put("newBalance", 1500000.0 - amount);
        response.put("receiptId", "RCP-" + System.currentTimeMillis());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("atmId", "ATM-SCL-001");
        response.put("security", Map.of(
            "transactionId", "TXN-" + System.currentTimeMillis(),
            "authorizationCode", "AUTH-" + (int)(Math.random() * 1000000),
            "encryptedCardData", "**** **** **** 9012"
        ));
        return Mono.just(response);
    }

    @PostMapping("/api/atm/deposit")
    public Mono<Map<String, Object>> atmDeposit(@RequestBody Map<String, Object> request) {
        Number amountNum = (Number) request.get("amount");
        Double amount = amountNum != null ? amountNum.doubleValue() : 0.0;

        if (amount <= 0 || amount > 5000000) {
            return Mono.just(Map.of(
                "success", false,
                "operationType", "DEPOSIT",
                "amount", amount,
                "error", "Amount must be between $1 and $5,000,000"
            ));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("operationType", "DEPOSIT");
        response.put("amount", amount);
        response.put("newBalance", 1500000.0 + amount);
        response.put("receiptId", "RCP-" + System.currentTimeMillis());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("atmId", "ATM-SCL-001");
        response.put("security", Map.of(
            "transactionId", "TXN-" + System.currentTimeMillis(),
            "authorizationCode", "AUTH-" + (int)(Math.random() * 1000000),
            "encryptedCardData", "**** **** **** 9012"
        ));
        return Mono.just(response);
    }

    @PostMapping("/api/atm/mini-statement")
    public Mono<Map<String, Object>> atmMiniStatement(@RequestBody Map<String, Object> request) {
        Map<String, Object> statement = new HashMap<>();
        statement.put("cardNumber", "**** **** **** 9012");
        statement.put("atmId", "ATM-SCL-001");
        statement.put("timestamp", LocalDateTime.now().toString());
        statement.put("balance", 1500000.0);
        statement.put("lastTransactions", List.of(
            Map.of("date", "06-10-25", "desc", "Retiro ATM", "amt", -50000.0),
            Map.of("date", "05-10-25", "desc", "Depósito", "amt", 200000.0),
            Map.of("date", "04-10-25", "desc", "Compra", "amt", -35000.0),
            Map.of("date", "03-10-25", "desc", "Transferencia", "amt", -100000.0),
            Map.of("date", "02-10-25", "desc", "Pago servicio", "amt", -45000.0)
        ));
        statement.put("receiptId", "RCP-" + System.currentTimeMillis());
        return Mono.just(statement);
    }

    @PostMapping("/api/atm/change-pin")
    public Mono<Map<String, Object>> atmChangePin(@RequestBody Map<String, Object> request) {
        String newPin = request.getOrDefault("newPin", "").toString();
        
        if (newPin.length() != 4 || !newPin.matches("\\d{4}")) {
            return Mono.just(Map.of(
                "success", false,
                "error", "PIN must be exactly 4 digits"
            ));
        }

        return Mono.just(Map.of(
            "success", true,
            "message", "PIN changed successfully",
            "atmId", "ATM-SCL-001",
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    @PostMapping("/api/atm/quick-transfer")
    public Mono<Map<String, Object>> atmQuickTransfer(@RequestBody Map<String, Object> request) {
        Number amountNum = (Number) request.get("amount");
        Double amount = amountNum != null ? amountNum.doubleValue() : 0.0;

        if (amount <= 0 || amount > 1000000) {
            return Mono.just(Map.of(
                "success", false,
                "operationType", "QUICK_TRANSFER",
                "amount", amount,
                "error", "ATM transfer limit is $1,000,000"
            ));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("operationType", "QUICK_TRANSFER");
        response.put("amount", amount);
        response.put("newBalance", 1500000.0 - amount);
        response.put("receiptId", "RCP-" + System.currentTimeMillis());
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("atmId", "ATM-SCL-001");
        response.put("security", Map.of(
            "transactionId", "TXN-" + System.currentTimeMillis(),
            "authorizationCode", "AUTH-" + (int)(Math.random() * 1000000),
            "encryptedCardData", "**** **** **** 9012"
        ));
        return Mono.just(response);
    }
}
