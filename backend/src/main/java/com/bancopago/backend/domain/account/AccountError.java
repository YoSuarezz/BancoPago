package com.bancopago.backend.domain.account;

import com.bancopago.backend.crosscutting.exception.ErrorCode;

public enum AccountError implements ErrorCode {
    NUMBER_EMPTY("El número de cuenta no puede estar vacío"),
    TYPE_REQUIRED("El tipo de cuenta es requerido"),
    OWNER_REQUIRED("El propietario de la cuenta es requerido"),
    NEGATIVE_INITIAL_BALANCE("El saldo inicial no puede ser negativo"),
    INVALID_AMOUNT("El monto %s no es válido. Debe ser un valor positivo."),
    INSUFFICIENT_BALANCE("Saldo insuficiente: actual=%s, requerido=%s"),
    BLOCKED("La cuenta %s se encuentra bloqueada"),
    INVALID_STATE("No se puede ejecutar '%s' en la cuenta %s con estado %s"),
    CURRENCY_MISMATCH("No se puede operar monedas diferentes: %s con %s");

    private final String messageTemplate;

    AccountError(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    @Override
    public String getCode() { return "ACCOUNT_" + name(); }
    @Override
    public String getMessageTemplate() { return messageTemplate; }
}
