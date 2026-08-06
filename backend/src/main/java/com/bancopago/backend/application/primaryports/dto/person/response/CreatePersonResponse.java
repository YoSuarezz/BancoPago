package com.bancopago.backend.application.primaryports.dto.person.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public sealed interface CreatePersonResponse
        permits CreateClientResponse, CreateEmployeeResponse {
}
