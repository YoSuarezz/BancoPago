package com.bancopago.backend.application.usecase.account.rulesvalidator.rules.impl;

import com.bancopago.backend.application.secondaryports.repository.PersonRepository;
import com.bancopago.backend.application.usecase.account.rulesvalidator.rules.OwnerExistsRule;
import com.bancopago.backend.domain.person.exceptions.PersonNotFoundException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class OwnerExistsRuleImpl implements OwnerExistsRule {

    private final PersonRepository personRepository;

    public OwnerExistsRuleImpl(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Mono<Void> validate(UUID ownerId) {
        return personRepository.findPersonById(ownerId)
                .switchIfEmpty(Mono.error(PersonNotFoundException.create(ownerId)))
                .then();
    }
}
