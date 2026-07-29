package com.bancopago.backend.application.primaryports.dto.person.request;

import com.bancopago.backend.domain.enums.DocumentType;
import com.bancopago.backend.domain.enums.PersonType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePersonRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank String documentNumber,
        @NotNull DocumentType documentType,
        @NotBlank @Email String email,
        String phone,
        @NotNull PersonType personType,
        String clientNumber,
        String position,
        String area,
        String costCenter,
        String contractType
) {
}
