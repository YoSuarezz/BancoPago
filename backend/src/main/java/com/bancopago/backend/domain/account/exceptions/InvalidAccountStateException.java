package com.bancopago.backend.domain.account.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.account.AccountError;
import com.bancopago.backend.domain.enums.AccountStatus;
import java.util.UUID;

public class InvalidAccountStateException extends DomainException {
    private static final long serialVersionUID = 1L;
    private final UUID accountId;
    private final AccountStatus currentStatus;
    private final String operation;

    private InvalidAccountStateException(UUID accountId, AccountStatus currentStatus, String operation) {
        super(AccountError.INVALID_STATE, operation, accountId, currentStatus);
        this.accountId = accountId;
        this.currentStatus = currentStatus;
        this.operation = operation;
    }

    public static InvalidAccountStateException create(UUID accountId, AccountStatus currentStatus, String operation) {
        return new InvalidAccountStateException(accountId, currentStatus, operation);
    }

    public UUID getAccountId() { return accountId; }
    public AccountStatus getCurrentStatus() { return currentStatus; }
    public String getOperation() { return operation; }
}
