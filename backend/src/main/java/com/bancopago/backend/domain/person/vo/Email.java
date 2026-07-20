package com.bancopago.backend.domain.person.vo;

import com.bancopago.backend.crosscutting.helpers.TextHelper;
import com.bancopago.backend.domain.person.PersonError;
import com.bancopago.backend.domain.person.exceptions.InvalidPersonException;

import java.util.regex.Pattern;

public record Email(String value) {

    private static final int MAX_LENGTH = 100;
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        value = TextHelper.applyTrim(value);
        if (TextHelper.isBlank(value)) {
            throw InvalidPersonException.create(PersonError.EMAIL_EMPTY);
        }
        value = value.toLowerCase();
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw InvalidPersonException.create(PersonError.EMAIL_INVALID);
        }
        if (value.length() > MAX_LENGTH) {
            throw InvalidPersonException.create(PersonError.EMAIL_EXCEEDED, MAX_LENGTH);
        }
    }
}
