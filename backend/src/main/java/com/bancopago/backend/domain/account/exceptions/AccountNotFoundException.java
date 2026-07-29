package com.bancopago.backend.domain.account.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.account.AccountError;

import java.io.Serial;
import java.util.UUID;

public class AccountNotFoundException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID accountId;

    private AccountNotFoundException(UUID accountId) {
        super(AccountError.NOT_FOUND, accountId);
        this.accountId = accountId;
    }

    public static AccountNotFoundException create(UUID accountId) {
        return new AccountNotFoundException(accountId);
    }

    public UUID getAccountId() {
        return accountId;
    }
}
