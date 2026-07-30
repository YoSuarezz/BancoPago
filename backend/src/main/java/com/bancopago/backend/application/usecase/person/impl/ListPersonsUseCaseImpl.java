package com.bancopago.backend.application.usecase.person.impl;

import com.bancopago.backend.application.secondaryports.repository.PersonRepository;
import com.bancopago.backend.application.usecase.person.ListPersonsUseCase;
import com.bancopago.backend.domain.person.PersonDomain;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class ListPersonsUseCaseImpl implements ListPersonsUseCase {

    private final PersonRepository personRepository;

    public ListPersonsUseCaseImpl(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Mono<List<PersonDomain>> execute() {
        return personRepository.findAllPersons().collectList();
    }
}
