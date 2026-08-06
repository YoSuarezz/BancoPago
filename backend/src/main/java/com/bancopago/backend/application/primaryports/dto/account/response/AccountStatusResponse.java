package com.bancopago.backend.application.primaryports.dto.account.response;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountStatusResponse(
        UUID id,
        String number,
        String status,
        BigDecimal balance
) {
}
