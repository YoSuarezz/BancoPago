package com.bancopago.backend.application.usecase.person;

import com.bancopago.backend.domain.person.PersonDomain;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetPersonByIdUseCase {

    Mono<PersonDomain> getPersonById(UUID personId);
}
