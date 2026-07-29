package com.bancopago.backend.application.primaryports.dto.person.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateClientResponse(
        UUID id,
        String name,
        String documentNumber,
        String documentType,
        String email,
        String phone,
        String personType,
        String clientNumber,
        LocalDate membershipDate
) implements CreatePersonResponse {
}
