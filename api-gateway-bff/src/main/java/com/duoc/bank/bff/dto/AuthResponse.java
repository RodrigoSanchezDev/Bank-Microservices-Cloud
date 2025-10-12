package com.duoc.bank.bff.dto;

public record AuthResponse(
    String token,
    String type,
    String username
) {}
