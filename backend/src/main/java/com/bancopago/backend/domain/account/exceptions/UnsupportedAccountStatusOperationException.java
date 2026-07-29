package com.bancopago.backend.domain.account.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.account.AccountError;
import com.bancopago.backend.domain.enums.AccountOperation;

import java.io.Serial;

/**
 * Operación no admitida por el caso de uso ChangeAccountStatus
 * (p. ej. OPERATE). Distinto de {@link InvalidAccountStateException},
 * que aplica cuando la cuenta existe pero su estado no permite la transición.
 */
public class UnsupportedAccountStatusOperationException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final AccountOperation operation;

    private UnsupportedAccountStatusOperationException(AccountOperation operation) {
        super(AccountError.OPERATION_NOT_ALLOWED, displayName(operation));
        this.operation = operation;
    }

    public static UnsupportedAccountStatusOperationException create(AccountOperation operation) {
        return new UnsupportedAccountStatusOperationException(operation);
    }

    public AccountOperation getOperation() {
        return operation;
    }

    private static String displayName(AccountOperation operation) {
        return operation == null ? "null" : operation.getDisplayName();
    }
}
