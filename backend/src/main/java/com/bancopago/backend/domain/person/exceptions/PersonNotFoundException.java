package com.bancopago.backend.domain.person.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.person.PersonError;

import java.io.Serial;
import java.util.UUID;

public class PersonNotFoundException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID personId;

    private PersonNotFoundException(UUID personId) {
        super(PersonError.NOT_FOUND, personId);
        this.personId = personId;
    }

    public static PersonNotFoundException create(UUID personId) {
        return new PersonNotFoundException(personId);
    }

    public UUID getPersonId() {
        return personId;
    }
}
