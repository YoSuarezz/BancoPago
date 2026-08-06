package com.bancopago.backend.application.primaryports.dto.person.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateEmployeeResponse(
        UUID id,
        String name,
        String documentNumber,
        String documentType,
        String email,
        String phone,
        String personType,
        String position,
        String area,
        String costCenter,
        String contractType
) implements CreatePersonResponse {
}
