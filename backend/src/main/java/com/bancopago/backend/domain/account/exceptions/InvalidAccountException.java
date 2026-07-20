package com.bancopago.backend.domain.account.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.account.AccountError;

public class InvalidAccountException extends DomainException {
    private static final long serialVersionUID = 1L;

    private InvalidAccountException(AccountError error, Object... args) {
        super(error, args);
    }

    public static InvalidAccountException create(AccountError error, Object... args) {
        return new InvalidAccountException(error, args);
    }
}
