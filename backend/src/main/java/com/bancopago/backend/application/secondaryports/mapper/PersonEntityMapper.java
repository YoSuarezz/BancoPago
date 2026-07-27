package com.bancopago.backend.application.secondaryports.mapper;

import com.bancopago.backend.application.secondaryports.entity.PersonEntity;
import com.bancopago.backend.domain.enums.DocumentType;
import com.bancopago.backend.domain.enums.PersonType;
import com.bancopago.backend.domain.person.ClientDomain;
import com.bancopago.backend.domain.person.EmployeeDomain;
import com.bancopago.backend.domain.person.PersonDomain;
import com.bancopago.backend.domain.person.vo.DocumentNumber;
import com.bancopago.backend.domain.person.vo.Email;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PersonEntityMapper {

    public PersonEntity toEntity(PersonDomain domain) {
        if (domain == null) {
            return null;
        }
        PersonEntity entity = new PersonEntity();
        entity.setId(domain.getId());
        entity.setNombre(domain.getName());
        entity.setDocumento(domain.getDocument());
        entity.setTipoDocumento(mapDocumentType(domain.getDocumentType()));
        entity.setEmail(domain.getEmail());
        entity.setTelefono(domain.getPhone());
        entity.setTipo(mapPersonType(domain.getPersonType()));
        entity.markNew();
        return entity;
    }

    public PersonDomain toDomain(PersonEntity entity) {
        if (entity == null) {
            return null;
        }
        var documentNumber = new DocumentNumber(mapDocumentType(entity.getTipoDocumento()), entity.getDocumento());
        var email = new Email(entity.getEmail());
        PersonType personType = mapPersonType(entity.getTipo());
        if (personType == PersonType.EMPLOYEE) {
            return new EmployeeDomain(
                    entity.getId(), documentNumber, entity.getNombre(),
                    email, entity.getTelefono(), null, null, null, null
            );
        }
        return new ClientDomain(
                entity.getId(), documentNumber, entity.getNombre(),
                email, entity.getTelefono(), null, null
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

    private String mapDocumentType(DocumentType type) {
        return type != null ? type.name() : null;
    }

    private DocumentType mapDocumentType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return DocumentType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String mapPersonType(PersonType type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case CLIENT -> "CLIENTE";
            case EMPLOYEE -> "EMPLEADO";
        };
    }

    private PersonType mapPersonType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return switch (value.trim().toUpperCase()) {
            case "CLIENTE", "CLIENT" -> PersonType.CLIENT;
            case "EMPLEADO", "EMPLOYEE" -> PersonType.EMPLOYEE;
            default -> null;
        };
    }
}
