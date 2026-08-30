package com.bancopago.backend.domain.auth.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.auth.UserError;

import java.io.Serial;

public class UserNotFoundException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    private UserNotFoundException(String email) {
        super(UserError.NOT_FOUND, email);
    }

    public static UserNotFoundException create(String email) {
        return new UserNotFoundException(email);
    }
}
