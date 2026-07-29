package com.bancopago.backend.application.usecase.account;

import com.bancopago.backend.domain.enums.AccountType;

import java.util.UUID;

/**
 * Entrada de aplicación para CreateAccount (no es DTO HTTP).
 * El número de cuenta se genera en el UseCase; por eso aún no hay AccountDomain completo.
 */
public record CreateAccountCommand(UUID ownerId, AccountType type) {
}
