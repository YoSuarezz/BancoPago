package com.bancopago.backend.application.primaryports.interactor.person;

import com.bancopago.backend.application.primaryports.dto.person.response.GetPersonByIdResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetPersonByIdInteractor {

    Mono<GetPersonByIdResponse> getPersonById(UUID personId);
}
