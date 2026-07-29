package com.bancopago.backend.application.primaryports.mapper.person;

import com.bancopago.backend.application.primaryports.dto.person.request.CreatePersonRequest;
import com.bancopago.backend.application.primaryports.dto.person.response.CreatePersonResponse;
import com.bancopago.backend.application.primaryports.dto.person.response.GetPersonByIdResponse;
import com.bancopago.backend.crosscutting.helpers.TextHelper;
import com.bancopago.backend.domain.enums.PersonType;
import com.bancopago.backend.domain.person.ClientDomain;
import com.bancopago.backend.domain.person.EmployeeDomain;
import com.bancopago.backend.domain.person.PersonDomain;
import com.bancopago.backend.domain.person.PersonError;
import com.bancopago.backend.domain.person.exceptions.InvalidPersonException;
import com.bancopago.backend.domain.person.vo.DocumentNumber;
import com.bancopago.backend.domain.person.vo.Email;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Híbrido: {@code toDomain} es factory de aplicación (Client/Employee + VOs);
 * Domain→Response usa métodos abstract generados por MapStruct (+ dispatch instanceof).
 */
@Mapper(componentModel = "spring")
public abstract class PersonDTOMapper {

    public PersonDomain toDomain(CreatePersonRequest request) {
        if (request.personType() == null) {
            throw InvalidPersonException.create(PersonError.TYPE_REQUIRED);
        }
        var documentNumber = new DocumentNumber(request.documentType(), request.documentNumber());
        var email = new Email(request.email());

        if (request.personType() == PersonType.EMPLOYEE) {
            return new EmployeeDomain(
                    request.name(),
                    documentNumber,
                    email,
                    request.phone(),
                    TextHelper.applyTrim(request.position()),
                    TextHelper.applyTrim(request.area()),
                    TextHelper.applyTrim(request.costCenter()),
                    TextHelper.applyTrim(request.contractType())
            );
        }
        return new ClientDomain(
                request.name(),
                documentNumber,
                email,
                request.phone(),
                TextHelper.applyTrim(request.clientNumber())
        );
    }

    public CreatePersonResponse toCreatePersonResponse(PersonDomain person) {
        if (person instanceof ClientDomain client) {
            return fromClientToCreateResponse(client);
        }
        if (person instanceof EmployeeDomain employee) {
            return fromEmployeeToCreateResponse(employee);
        }
        throw InvalidPersonException.create(PersonError.TYPE_REQUIRED);
    }

    public GetPersonByIdResponse toGetPersonByIdResponse(PersonDomain person) {
        if (person instanceof ClientDomain client) {
            return fromClientToGetResponse(client);
        }
        if (person instanceof EmployeeDomain employee) {
            return fromEmployeeToGetResponse(employee);
        }
        throw InvalidPersonException.create(PersonError.TYPE_REQUIRED);
    }

    @Mapping(source = "document", target = "documentNumber")
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "area", ignore = true)
    @Mapping(target = "costCenter", ignore = true)
    @Mapping(target = "contractType", ignore = true)
    protected abstract CreatePersonResponse fromClientToCreateResponse(ClientDomain client);

    @Mapping(source = "document", target = "documentNumber")
    @Mapping(target = "clientNumber", ignore = true)
    @Mapping(target = "membershipDate", ignore = true)
    protected abstract CreatePersonResponse fromEmployeeToCreateResponse(EmployeeDomain employee);

    @Mapping(source = "document", target = "documentNumber")
    @Mapping(target = "position", ignore = true)
    @Mapping(target = "area", ignore = true)
    @Mapping(target = "costCenter", ignore = true)
    @Mapping(target = "contractType", ignore = true)
    protected abstract GetPersonByIdResponse fromClientToGetResponse(ClientDomain client);

    @Mapping(source = "document", target = "documentNumber")
    @Mapping(target = "clientNumber", ignore = true)
    @Mapping(target = "membershipDate", ignore = true)
    protected abstract GetPersonByIdResponse fromEmployeeToGetResponse(EmployeeDomain employee);
}
