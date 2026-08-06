package com.bancopago.backend.domain.auth.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.auth.UserError;

import java.io.Serial;

public class InvalidCredentialsException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    private InvalidCredentialsException() {
        super(UserError.INVALID_CREDENTIALS);
    }

    public static InvalidCredentialsException create() {
        return new InvalidCredentialsException();
    }
}
