package com.bancopago.backend.application.usecase.person.impl;

import com.bancopago.backend.application.secondaryports.repository.PersonRepository;
import com.bancopago.backend.application.usecase.person.CreatePersonUseCase;
import com.bancopago.backend.application.usecase.person.rulesvalidator.CreatePersonRulesValidator;
import com.bancopago.backend.domain.person.PersonDomain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
public class CreatePersonUseCaseImpl implements CreatePersonUseCase {

    private final PersonRepository personRepository;
    private final CreatePersonRulesValidator rulesValidator;

    public CreatePersonUseCaseImpl(PersonRepository personRepository,
                                   CreatePersonRulesValidator rulesValidator) {
        this.personRepository = personRepository;
        this.rulesValidator = rulesValidator;
    }

    @Override
    @Transactional
    public Mono<PersonDomain> execute(PersonDomain person) {
        return rulesValidator.validate(person)
                .then(Mono.defer(() -> personRepository.savePerson(person)));
    }
}
