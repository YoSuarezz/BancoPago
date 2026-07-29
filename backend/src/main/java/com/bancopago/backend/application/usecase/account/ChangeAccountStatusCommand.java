package com.bancopago.backend.application.usecase.account;

import com.bancopago.backend.domain.enums.AccountOperation;

import java.util.UUID;

public record ChangeAccountStatusCommand(UUID accountId, AccountOperation operation) {
}
