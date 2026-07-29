package com.bancopago.backend.application.primaryports.dto.account.response;

import java.math.BigDecimal;
import java.util.UUID;

public record GetAccountBalanceResponse(
        UUID accountId,
        String accountNumber,
        BigDecimal balance,
        String currency,
        String status
) {
}
