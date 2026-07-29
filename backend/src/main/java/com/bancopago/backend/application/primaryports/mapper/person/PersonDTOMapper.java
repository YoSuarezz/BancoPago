package com.bancopago.backend.application.primaryports.mapper.person;

import com.bancopago.backend.application.primaryports.dto.person.request.CreatePersonRequest;
import com.bancopago.backend.application.primaryports.dto.person.response.CreateClientResponse;
import com.bancopago.backend.application.primaryports.dto.person.response.CreateEmployeeResponse;
import com.bancopago.backend.application.primaryports.dto.person.response.CreatePersonResponse;
import com.bancopago.backend.application.primaryports.dto.person.response.GetClientByIdResponse;
import com.bancopago.backend.application.primaryports.dto.person.response.GetEmployeeByIdResponse;
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

@Mapper(componentModel = "spring")
public abstract class PersonDTOMapper {

    public PersonDomain toPersonDomain(CreatePersonRequest request) {
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
            return new CreateClientResponse(
                    client.getId(),
                    client.getName(),
                    client.getDocument(),
                    enumName(client.getDocumentType()),
                    client.getEmail(),
                    blankToNull(client.getPhone()),
                    enumName(client.getPersonType()),
                    blankToNull(client.getClientNumber()),
                    client.getMembershipDate()
            );
        }
        if (person instanceof EmployeeDomain employee) {
            return new CreateEmployeeResponse(
                    employee.getId(),
                    employee.getName(),
                    employee.getDocument(),
                    enumName(employee.getDocumentType()),
                    employee.getEmail(),
                    blankToNull(employee.getPhone()),
                    enumName(employee.getPersonType()),
                    blankToNull(employee.getPosition()),
                    blankToNull(employee.getArea()),
                    blankToNull(employee.getCostCenter()),
                    blankToNull(employee.getContractType())
            );
        }
        throw InvalidPersonException.create(PersonError.TYPE_REQUIRED);
    }

    public GetPersonByIdResponse toGetPersonByIdResponse(PersonDomain person) {
        if (person instanceof ClientDomain client) {
            return new GetClientByIdResponse(
                    client.getId(),
                    client.getName(),
                    client.getDocument(),
                    enumName(client.getDocumentType()),
                    client.getEmail(),
                    blankToNull(client.getPhone()),
                    enumName(client.getPersonType()),
                    blankToNull(client.getClientNumber()),
                    client.getMembershipDate()
            );
        }
        if (person instanceof EmployeeDomain employee) {
            return new GetEmployeeByIdResponse(
                    employee.getId(),
                    employee.getName(),
                    employee.getDocument(),
                    enumName(employee.getDocumentType()),
                    employee.getEmail(),
                    blankToNull(employee.getPhone()),
                    enumName(employee.getPersonType()),
                    blankToNull(employee.getPosition()),
                    blankToNull(employee.getArea()),
                    blankToNull(employee.getCostCenter()),
                    blankToNull(employee.getContractType())
            );
        }
        throw InvalidPersonException.create(PersonError.TYPE_REQUIRED);
    }

    private static String enumName(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static String blankToNull(String value) {
        return TextHelper.isBlank(value) ? null : value;
    }
}
