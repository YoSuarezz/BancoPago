package com.bancopago.backend.application.primaryports.dto.account.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ChangeAccountStatusResponse(
        UUID id,
        UUID ownerId,
        String number,
        String type,
        BigDecimal balance,
        String currency,
        String status
) {
}
