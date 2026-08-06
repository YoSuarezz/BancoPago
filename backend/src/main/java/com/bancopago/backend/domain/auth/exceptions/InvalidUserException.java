package com.bancopago.backend.domain.auth.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.auth.UserError;

import java.io.Serial;

public class InvalidUserException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    private InvalidUserException(UserError error) {
        super(error);
    }

    public static InvalidUserException create(UserError error) {
        return new InvalidUserException(error);
    }
}
