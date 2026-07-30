package com.bancopago.backend.application.primaryports.dto.person.response;

import java.util.UUID;

public record ListPersonResponse(
        UUID id,
        String name,
        String documentNumber,
        String documentType,
        String personType
) {
}
