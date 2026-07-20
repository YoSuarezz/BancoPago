package com.bancopago.backend.domain.person;

import com.bancopago.backend.domain.enums.PersonType;
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
        this.clientNumber = clientNumber;
        this.membershipDate = membershipDate != null ? membershipDate : LocalDate.now();
    }

    public ClientDomain(String name, DocumentNumber documentNumber,
                        Email email, String phone, String clientNumber) {
        this(null, documentNumber, name, email, phone, clientNumber, LocalDate.now());
    }

    public String getClientNumber() { return clientNumber; }
    public LocalDate getMembershipDate() { return membershipDate; }
}
