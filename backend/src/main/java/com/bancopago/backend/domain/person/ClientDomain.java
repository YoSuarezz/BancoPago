package com.bancopago.backend.domain.person;

import com.bancopago.backend.crosscutting.helpers.TextHelper;
import com.bancopago.backend.domain.enums.PersonType;
import com.bancopago.backend.domain.person.exceptions.InvalidPersonException;
import com.bancopago.backend.domain.person.vo.DocumentNumber;
import com.bancopago.backend.domain.person.vo.Email;

import java.time.LocalDate;
import java.util.UUID;

public class ClientDomain extends PersonDomain {

    private final String clientNumber;
    private final LocalDate membershipDate;

    public ClientDomain(UUID id, DocumentNumber documentNumber, String name,
                        Email email, String phone,
                        String clientNumber, LocalDate membershipDate) {
        super(id, documentNumber, name, email, phone, PersonType.CLIENT);
        this.clientNumber = requireClientNumber(clientNumber);
        this.membershipDate = membershipDate != null ? membershipDate : LocalDate.now();
    }

    public ClientDomain(String name, DocumentNumber documentNumber,
                        Email email, String phone, String clientNumber) {
        this(null, documentNumber, name, email, phone, clientNumber, LocalDate.now());
    }

    private static String requireClientNumber(String clientNumber) {
        var trimmed = TextHelper.applyTrim(clientNumber);
        if (TextHelper.isBlank(trimmed)) {
            throw InvalidPersonException.create(PersonError.CLIENT_NUMBER_REQUIRED);
        }
        return trimmed;
    }

    public String getClientNumber() { return clientNumber; }
    public LocalDate getMembershipDate() { return membershipDate; }
}
