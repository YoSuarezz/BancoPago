package com.bancopago.backend.domain.auth.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.auth.UserError;

import java.io.Serial;

public class DuplicateUserEmailException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    private DuplicateUserEmailException(String email) {
        super(UserError.EMAIL_ALREADY_EXISTS, email);
    }

    public static DuplicateUserEmailException create(String email) {
        return new DuplicateUserEmailException(email);
    }
}
