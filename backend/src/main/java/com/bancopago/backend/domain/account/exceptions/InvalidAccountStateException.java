package com.bancopago.backend.domain.account.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.account.AccountError;
import com.bancopago.backend.domain.enums.AccountOperation;
import com.bancopago.backend.domain.enums.AccountStatus;

import java.io.Serial;
import java.util.UUID;

public class InvalidAccountStateException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;
    private final UUID accountId;
    private final AccountStatus currentStatus;
    private final AccountOperation operation;

    private InvalidAccountStateException(UUID accountId, AccountStatus currentStatus, AccountOperation operation) {
        super(AccountError.INVALID_STATE, operation.getDisplayName(), accountId, currentStatus);
        this.accountId = accountId;
        this.currentStatus = currentStatus;
        this.operation = operation;
    }

    public static InvalidAccountStateException create(UUID accountId, AccountStatus currentStatus, AccountOperation operation) {
        return new InvalidAccountStateException(accountId, currentStatus, operation);
    }

    public UUID getAccountId() { return accountId; }
    public AccountStatus getCurrentStatus() { return currentStatus; }
    public AccountOperation getOperation() { return operation; }
}
