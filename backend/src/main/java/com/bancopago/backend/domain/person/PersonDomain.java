package com.bancopago.backend.domain.person;

import com.bancopago.backend.crosscutting.helpers.ObjectHelper;
import com.bancopago.backend.crosscutting.helpers.TextHelper;
import com.bancopago.backend.domain.BaseDomain;
import com.bancopago.backend.domain.enums.DocumentType;
import com.bancopago.backend.domain.enums.PersonType;
import com.bancopago.backend.domain.person.exceptions.InvalidPersonException;
import com.bancopago.backend.domain.person.vo.DocumentNumber;
import com.bancopago.backend.domain.person.vo.Email;

import java.util.UUID;

public abstract class PersonDomain extends BaseDomain {

    private static final int MAX_NAME_LENGTH = 100;

    private final DocumentNumber documentNumber;
    private final String name;
    private final Email email;
    private final String phone;
    private final PersonType type;

    protected PersonDomain(UUID id, DocumentNumber documentNumber, String name,
                           Email email, String phone, PersonType type) {
        super(id);
        this.documentNumber = ObjectHelper.requireNonNull(documentNumber,
                () -> InvalidPersonException.create(PersonError.DOCUMENT_TYPE_REQUIRED));
        this.name = validateName(name);
        this.email = ObjectHelper.requireNonNull(email,
                () -> InvalidPersonException.create(PersonError.EMAIL_EMPTY));
        this.phone = TextHelper.applyTrim(phone);
        this.type = ObjectHelper.requireNonNull(type,
                () -> InvalidPersonException.create(PersonError.TYPE_REQUIRED));
    }

    private static String validateName(String name) {
        var trimmed = TextHelper.applyTrim(name);
        if (TextHelper.isBlank(trimmed)) {
            throw InvalidPersonException.create(PersonError.NAME_EMPTY);
        }
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw InvalidPersonException.create(PersonError.NAME_EXCEEDED, MAX_NAME_LENGTH);
        }
        return trimmed;
    }

    public DocumentNumber getDocumentNumber() { return documentNumber; }
    public DocumentType getDocumentType() { return documentNumber.type(); }
    public String getDocument() { return documentNumber.value(); }
    public String getName() { return name; }
    public Email getEmailObject() { return email; }
    public String getEmail() { return email.value(); }
    public String getPhone() { return phone; }
    public PersonType getPersonType() { return type; }
}
