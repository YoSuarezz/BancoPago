package com.bancopago.backend.application.model;

import com.bancopago.backend.crosscutting.helpers.TextHelper;
import com.bancopago.backend.domain.enums.PersonType;

public record PersonQuery(
        int page,
        int size,
        String sortBy,
        String sortDirection,
        String name,
        PersonType personType
) {
    public PersonQuery {
        page = Math.max(page, 0);
        size = Math.max(size, 1);
        sortBy = TextHelper.isBlank(sortBy) ? "name" : TextHelper.applyTrim(sortBy);
        sortDirection = TextHelper.isBlank(sortDirection) ? "ASC" : TextHelper.applyTrim(sortDirection);
        name = TextHelper.applyTrim(name);
    }
}
