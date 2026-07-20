package com.bancopago.backend.domain.person.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.person.PersonError;

public class InvalidPersonException extends DomainException {
    private static final long serialVersionUID = 1L;

    private InvalidPersonException(PersonError error, Object... args) {
        super(error, args);
    }

    public static InvalidPersonException create(PersonError error, Object... args) {
        return new InvalidPersonException(error, args);
    }
}
