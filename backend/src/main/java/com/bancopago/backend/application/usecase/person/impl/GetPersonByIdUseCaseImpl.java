package com.bancopago.backend.application.usecase.person.impl;

import com.bancopago.backend.application.secondaryports.repository.PersonRepository;
import com.bancopago.backend.application.usecase.person.GetPersonByIdUseCase;
import com.bancopago.backend.domain.person.PersonDomain;
import com.bancopago.backend.domain.person.exceptions.PersonNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class GetPersonByIdUseCaseImpl implements GetPersonByIdUseCase {

    private final PersonRepository personRepository;

    public GetPersonByIdUseCaseImpl(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Mono<PersonDomain> getPersonById(UUID personId) {
        return personRepository.findPersonById(personId)
                .switchIfEmpty(Mono.error(PersonNotFoundException.create(personId)));
    }
}
