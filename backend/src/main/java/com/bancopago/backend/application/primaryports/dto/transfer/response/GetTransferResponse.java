package com.bancopago.backend.application.primaryports.dto.transfer.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record GetTransferResponse(
        UUID id,
        String sourceAccountNumber,
        String targetAccountNumber,
        BigDecimal amount,
        String currency,
        String status,
        String description,
        LocalDateTime createdAt
) {
}
