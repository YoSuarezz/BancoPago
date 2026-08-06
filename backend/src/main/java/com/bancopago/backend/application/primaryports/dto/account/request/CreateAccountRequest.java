package com.bancopago.backend.application.primaryports.dto.account.request;

import com.bancopago.backend.domain.enums.AccountType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateAccountRequest(
        @NotNull UUID ownerId,
        @NotNull AccountType type
) {
}
