package com.bancopago.backend.domain.auth;

import com.bancopago.backend.crosscutting.exception.ErrorCode;

public enum UserError implements ErrorCode {
    EMAIL_REQUIRED("El email del usuario es requerido"),
    PASSWORD_REQUIRED("La contraseña es requerida"),
    ROLE_REQUIRED("El rol del usuario es requerido"),
    EMAIL_ALREADY_EXISTS("Ya existe un usuario con el email %s"),
    NOT_FOUND("No se encontró el usuario con email %s"),
    INVALID_CREDENTIALS("Credenciales inválidas"),
    INVALID_TOKEN("Token de autenticación inválido o expirado");

    private final String messageTemplate;

    UserError(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    @Override
    public String getCode() { return "AUTH_" + name(); }
    @Override
    public String getMessageTemplate() { return messageTemplate; }
}
