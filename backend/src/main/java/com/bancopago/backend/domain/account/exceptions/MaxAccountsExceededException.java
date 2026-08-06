package com.bancopago.backend.domain.account.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.account.AccountError;

import java.io.Serial;
import java.util.UUID;

public class MaxAccountsExceededException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID ownerId;
    private final int maxAllowed;

    private MaxAccountsExceededException(UUID ownerId, int maxAllowed) {
        super(AccountError.MAX_ACCOUNTS_EXCEEDED, ownerId, maxAllowed);
        this.ownerId = ownerId;
        this.maxAllowed = maxAllowed;
    }

    public static MaxAccountsExceededException create(UUID ownerId, int maxAllowed) {
        return new MaxAccountsExceededException(ownerId, maxAllowed);
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public int getMaxAllowed() {
        return maxAllowed;
    }
}
