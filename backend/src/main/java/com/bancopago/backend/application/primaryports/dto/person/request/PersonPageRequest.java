package com.bancopago.backend.application.primaryports.dto.person.request;

import com.bancopago.backend.domain.enums.PersonType;

public record PersonPageRequest(
        int page,
        int size,
        String sortBy,
        String sortDirection,
        String name,
        PersonType personType
) {
    public static PersonType parsePersonType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return PersonType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
