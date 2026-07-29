package com.bancopago.backend.application.usecase.person.rulesvalidator.impl;

import com.bancopago.backend.application.usecase.person.rulesvalidator.CreatePersonRulesValidator;
import com.bancopago.backend.application.usecase.person.rulesvalidator.rules.UniqueDocumentRule;
import com.bancopago.backend.application.usecase.person.rulesvalidator.rules.UniqueEmailRule;
import com.bancopago.backend.domain.person.PersonDomain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CreatePersonRulesValidatorImpl implements CreatePersonRulesValidator {

    private final UniqueDocumentRule uniqueDocumentRule;
    private final UniqueEmailRule uniqueEmailRule;

    public CreatePersonRulesValidatorImpl(UniqueDocumentRule uniqueDocumentRule,
                                          UniqueEmailRule uniqueEmailRule) {
        this.uniqueDocumentRule = uniqueDocumentRule;
        this.uniqueEmailRule = uniqueEmailRule;
    }

    @Override
    public Mono<Void> validate(PersonDomain person) {
        return uniqueDocumentRule.validate(person.getDocumentNumber())
                .then(uniqueEmailRule.validate(person.getEmail()));
    }
}
