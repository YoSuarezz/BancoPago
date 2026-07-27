package com.bancopago.backend.application.secondaryports.mapper;

import com.bancopago.backend.application.secondaryports.entity.PersonEntity;
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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manual Entity ↔ Domain mapper.
 * Kept manual (not MapStruct) because {@link PersonDomain} is abstract and must be
 * reconstituted as {@link ClientDomain} or {@link EmployeeDomain}, and because
 * persistence columns map to Value Objects ({@link DocumentNumber}, {@link Email}).
 * <p>
 * Subclass-only fields (clientNumber, position, etc.) are not stored in {@code person}
 * yet, so reconstitution uses safe defaults rather than inventing persisted values.
 */
@Component
public class PersonEntityMapper {

    public PersonEntity toEntity(PersonDomain domain) {
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
        entity.markNew();
        return entity;
    }

    public PersonDomain toDomain(PersonEntity entity) {
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
                    TextHelper.EMPTY,
                    TextHelper.EMPTY,
                    TextHelper.EMPTY,
                    TextHelper.EMPTY
            );
        }
        return new ClientDomain(
                entity.getId(),
                documentNumber,
                entity.getName(),
                email,
                entity.getPhone(),
                TextHelper.EMPTY,
                null
        );
    }

    public List<PersonEntity> toEntityCollection(List<PersonDomain> domainList) {
        if (domainList == null) {
            return List.of();
        }
        return domainList.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public List<PersonDomain> toDomainCollection(List<PersonEntity> entityList) {
        if (entityList == null) {
            return List.of();
        }
        return entityList.stream().map(this::toDomain).collect(Collectors.toList());
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
