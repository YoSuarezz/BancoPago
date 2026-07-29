package com.bancopago.backend.application.usecase.person.rulesvalidator.rules.impl;

import com.bancopago.backend.application.secondaryports.repository.PersonRepository;
import com.bancopago.backend.application.usecase.person.rulesvalidator.rules.UniqueEmailRule;
import com.bancopago.backend.domain.person.exceptions.DuplicateEmailException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UniqueEmailRuleImpl implements UniqueEmailRule {

    private final PersonRepository personRepository;

    public UniqueEmailRuleImpl(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Mono<Void> validate(String email) {
        return personRepository.existsPersonByEmail(email)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(DuplicateEmailException.create(email));
                    }
                    return Mono.empty();
                });
    }
}
