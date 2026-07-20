package com.bancopago.backend.domain.person.vo;

import com.bancopago.backend.crosscutting.helpers.TextHelper;
import com.bancopago.backend.domain.enums.DocumentType;
import com.bancopago.backend.domain.person.PersonError;
import com.bancopago.backend.domain.person.exceptions.InvalidPersonException;

public record DocumentNumber(DocumentType type, String value) {

    private static final int MAX_LENGTH = 30;

    public DocumentNumber {
        if (type == null) {
            throw InvalidPersonException.create(PersonError.DOCUMENT_TYPE_REQUIRED);
        }
        value = TextHelper.applyTrim(value);
        if (TextHelper.isBlank(value)) {
            throw InvalidPersonException.create(PersonError.DOCUMENT_EMPTY);
        }
        if (value.length() > MAX_LENGTH) {
            throw InvalidPersonException.create(PersonError.DOCUMENT_EXCEEDED, MAX_LENGTH);
        }
    }
}
