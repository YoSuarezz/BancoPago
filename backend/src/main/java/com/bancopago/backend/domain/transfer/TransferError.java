package com.bancopago.backend.domain.transfer;

import com.bancopago.backend.crosscutting.exception.ErrorCode;

public enum TransferError implements ErrorCode {
    SAME_ACCOUNT("No se puede transferir a la misma cuenta"),
    SOURCE_NOT_FOUND("No se encontró la cuenta origen con número %s"),
    TARGET_NOT_FOUND("No se encontró la cuenta destino con número %s"),
    NOT_FOUND("No se encontró la transferencia con id %s"),
    INVALID_AMOUNT("El monto de transferencia debe ser positivo"),
    DESCRIPTION_TOO_LONG("La descripción no puede superar %d caracteres"),
    INVALID_STATUS_TRANSITION("No se puede cambiar el estado de la transferencia de %s a %s"),
    SOURCE_REQUIRED("El número de cuenta origen es requerido"),
    TARGET_REQUIRED("El número de cuenta destino es requerido"),
    IDEMPOTENCY_KEY_REQUIRED("La clave de idempotencia es requerida"),
    INSUFFICIENT_BALANCE("Saldo insuficiente en la cuenta origen %s"),
    SOURCE_NOT_OPERABLE("La cuenta origen %s no está activa para realizar transferencias"),
    DUPLICATE_IDEMPOTENCY_KEY("Ya existe una transferencia con la clave de idempotencia %s");

    private final String messageTemplate;

    TransferError(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    @Override
    public String getCode() { return "TRANSFER_" + name(); }
    @Override
    public String getMessageTemplate() { return messageTemplate; }
}
