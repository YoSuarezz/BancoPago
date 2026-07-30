package com.bancopago.backend.application.usecase.account;

import com.bancopago.backend.domain.enums.AccountType;

import java.util.UUID;

public record CreateAccountCommand(UUID ownerId, AccountType type) {
}
