package com.bancopago.backend.domain.person.exceptions;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.person.PersonError;

import java.io.Serial;

public class DuplicateDocumentException extends DomainException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String documentNumber;

    private DuplicateDocumentException(String documentNumber) {
        super(PersonError.DOCUMENT_ALREADY_EXISTS, documentNumber);
        this.documentNumber = documentNumber;
    }

    public static DuplicateDocumentException create(String documentNumber) {
        return new DuplicateDocumentException(documentNumber);
    }

    public String getDocumentNumber() {
        return documentNumber;
    }
}
