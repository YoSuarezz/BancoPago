package com.bancopago.backend.application.usecase.transfer;

import java.math.BigDecimal;

public record TransferCommand(
        String sourceAccountNumber,
        String targetAccountNumber,
        BigDecimal amount,
        String description,
        String idempotencyKey
) {
}
