package com.bancopago.backend.application.primaryports.dto.auth.response;

import com.bancopago.backend.domain.enums.UserRole;

public record AuthResponse(
        String token,
        String tokenType,
        String email,
        UserRole role
) {
    public static AuthResponse of(String token, String email, UserRole role) {
        return new AuthResponse(token, "Bearer", email, role);
    }
}
