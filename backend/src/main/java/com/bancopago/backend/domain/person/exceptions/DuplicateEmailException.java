package com.bancopago.backend.domain.person.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.person.PersonError;

import java.io.Serial;

public class DuplicateEmailException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String email;

    private DuplicateEmailException(String email) {
        super(PersonError.EMAIL_ALREADY_EXISTS, email);
        this.email = email;
    }

    public static DuplicateEmailException create(String email) {
        return new DuplicateEmailException(email);
    }

    public String getEmail() {
        return email;
    }
}
