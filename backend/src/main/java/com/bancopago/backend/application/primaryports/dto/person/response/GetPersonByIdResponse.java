package com.bancopago.backend.application.primaryports.dto.person.response;

import java.time.LocalDate;
import java.util.UUID;

public record GetPersonByIdResponse(
        UUID id,
        String name,
        String documentNumber,
        String documentType,
        String email,
        String phone,
        String personType,
        String clientNumber,
        LocalDate membershipDate,
        String position,
        String area,
        String costCenter,
        String contractType
) {
}
