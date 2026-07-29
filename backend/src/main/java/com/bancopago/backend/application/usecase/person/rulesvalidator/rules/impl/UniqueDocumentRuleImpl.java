package com.bancopago.backend.application.usecase.person.rulesvalidator.rules.impl;

import com.bancopago.backend.application.secondaryports.repository.PersonRepository;
import com.bancopago.backend.application.usecase.person.rulesvalidator.rules.UniqueDocumentRule;
import com.bancopago.backend.domain.person.exceptions.DuplicateDocumentException;
import com.bancopago.backend.domain.person.vo.DocumentNumber;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UniqueDocumentRuleImpl implements UniqueDocumentRule {

    private final PersonRepository personRepository;

    public UniqueDocumentRuleImpl(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    @Override
    public Mono<Void> validate(DocumentNumber document) {
        return personRepository.existsPersonByDocument(document.value(), document.type().name())
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(DuplicateDocumentException.create(document.value()));
                    }
                    return Mono.empty();
                });
    }
}
