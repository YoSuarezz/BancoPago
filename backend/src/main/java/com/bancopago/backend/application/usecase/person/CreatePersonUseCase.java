package com.bancopago.backend.application.usecase.person;

import com.bancopago.backend.domain.person.PersonDomain;
import reactor.core.publisher.Mono;

public interface CreatePersonUseCase {

    Mono<PersonDomain> createPerson(PersonDomain person);
}
