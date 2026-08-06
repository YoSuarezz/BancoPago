package com.bancopago.backend.domain.account.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.account.AccountError;

import java.io.Serial;
import java.util.UUID;

public class AccountBlockedException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;
    private final UUID accountId;

    private AccountBlockedException(UUID accountId) {
        super(AccountError.BLOCKED, accountId);
        this.accountId = accountId;
    }

    public static AccountBlockedException create(UUID accountId) {
        return new AccountBlockedException(accountId);
    }

    public UUID getAccountId() { return accountId; }
}
