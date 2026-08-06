package com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.mapper;

import com.bancopago.backend.crosscutting.helpers.TextHelper;
import com.bancopago.backend.domain.enums.DocumentType;
import com.bancopago.backend.domain.enums.PersonType;
import com.bancopago.backend.domain.person.ClientDomain;
import com.bancopago.backend.domain.person.EmployeeDomain;
import com.bancopago.backend.domain.person.PersonDomain;
import com.bancopago.backend.domain.person.PersonError;
import com.bancopago.backend.domain.person.exceptions.InvalidPersonException;
import com.bancopago.backend.domain.person.vo.DocumentNumber;
import com.bancopago.backend.domain.person.vo.Email;
import com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.entity.PersonEntity;
import org.springframework.stereotype.Component;

@Component
public class PersonEntityMapper {

    public PersonEntity toPersonEntity(PersonDomain domain) {
        if (domain == null) {
            return null;
        }
        PersonEntity entity = new PersonEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setDocumentNumber(domain.getDocument());
        entity.setDocumentType(requireEnumName(domain.getDocumentType(), PersonError.DOCUMENT_TYPE_REQUIRED));
        entity.setEmail(domain.getEmail());
        entity.setPhone(domain.getPhone());
        entity.setPersonType(requireEnumName(domain.getPersonType(), PersonError.TYPE_REQUIRED));

        if (domain instanceof ClientDomain client) {
            entity.setClientNumber(client.getClientNumber());
            entity.setMembershipDate(client.getMembershipDate());
        } else if (domain instanceof EmployeeDomain employee) {
            entity.setPosition(employee.getPosition());
            entity.setArea(employee.getArea());
            entity.setCostCenter(employee.getCostCenter());
            entity.setContractType(employee.getContractType());
        }

        entity.markNew();
        return entity;
    }

    public PersonDomain toPersonDomain(PersonEntity entity) {
        if (entity == null) {
            return null;
        }
        var documentNumber = new DocumentNumber(
                parseDocumentType(entity.getDocumentType()),
                entity.getDocumentNumber()
        );
        var email = new Email(entity.getEmail());
        PersonType personType = parsePersonType(entity.getPersonType());

        if (personType == PersonType.EMPLOYEE) {
            return new EmployeeDomain(
                    entity.getId(),
                    documentNumber,
                    entity.getName(),
                    email,
                    entity.getPhone(),
                    entity.getPosition(),
                    entity.getArea(),
                    entity.getCostCenter(),
                    entity.getContractType()
            );
        }
        return new ClientDomain(
                entity.getId(),
                documentNumber,
                entity.getName(),
                email,
                entity.getPhone(),
                entity.getClientNumber(),
                entity.getMembershipDate()
        );
    }

    private String requireEnumName(Enum<?> value, PersonError error) {
        if (value == null) {
            throw InvalidPersonException.create(error);
        }
        return value.name();
    }

    private DocumentType parseDocumentType(String value) {
        if (TextHelper.isBlank(value)) {
            throw InvalidPersonException.create(PersonError.DOCUMENT_TYPE_REQUIRED);
        }
        try {
            return DocumentType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw InvalidPersonException.create(PersonError.DOCUMENT_TYPE_REQUIRED);
        }
    }

    private PersonType parsePersonType(String value) {
        if (TextHelper.isBlank(value)) {
            throw InvalidPersonException.create(PersonError.TYPE_REQUIRED);
        }
        try {
            return PersonType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw InvalidPersonException.create(PersonError.TYPE_REQUIRED);
        }
    }
}
