package com.bancopago.backend.application.primaryports.interactor.person;

import com.bancopago.backend.application.primaryports.dto.person.request.CreatePersonRequest;
import com.bancopago.backend.application.primaryports.dto.person.response.CreatePersonResponse;
import reactor.core.publisher.Mono;

public interface CreatePersonInteractor {

    Mono<CreatePersonResponse> createPerson(CreatePersonRequest request);
}
