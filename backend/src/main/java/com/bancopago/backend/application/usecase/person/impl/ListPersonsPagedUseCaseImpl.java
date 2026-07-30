package com.bancopago.backend.application.usecase.person.impl;

import com.bancopago.backend.application.model.PageResult;
import com.bancopago.backend.application.model.PersonQuery;
import com.bancopago.backend.application.secondaryports.repository.PersonRepository;
import com.bancopago.backend.application.usecase.person.ListPersonsPagedUseCase;
import com.bancopago.backend.domain.person.PersonDomain;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ListPersonsPagedUseCaseImpl implements ListPersonsPagedUseCase {

    private final PersonRepository personRepository;

    public ListPersonsPagedUseCaseImpl(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Mono<PageResult<PersonDomain>> execute(PersonQuery query) {
        return personRepository.findPersonsPage(query);
    }
}
