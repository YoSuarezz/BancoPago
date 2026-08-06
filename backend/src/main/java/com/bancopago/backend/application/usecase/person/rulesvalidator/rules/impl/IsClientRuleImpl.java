package com.bancopago.backend.application.usecase.person.rulesvalidator.rules.impl;

import com.bancopago.backend.application.usecase.person.rulesvalidator.rules.IsClientRule;
import com.bancopago.backend.domain.enums.PersonType;
import com.bancopago.backend.domain.person.PersonError;
import com.bancopago.backend.domain.person.exceptions.InvalidPersonException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class IsClientRuleImpl implements IsClientRule {

    @Override
    public Mono<Void> validate(PersonType personType) {
        if (personType != PersonType.CLIENT) {
            return Mono.error(InvalidPersonException.create(PersonError.TYPE_REQUIRED));
        }
        return Mono.empty();
    }
}
