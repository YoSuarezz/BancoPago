package com.bancopago.backend.application.primaryports.dto.person.request;

import com.bancopago.backend.domain.enums.DocumentType;
import com.bancopago.backend.domain.enums.PersonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePersonRequest(
        @NotBlank String name,
        @NotBlank String documentNumber,
        @NotNull DocumentType documentType,
        @NotBlank String email,
        String phone,
        @NotNull PersonType personType,
        String clientNumber,
        String position,
        String area,
        String costCenter,
        String contractType
) {
}
