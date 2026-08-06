package com.bancopago.backend.application.usecase.person.impl;

import com.bancopago.backend.application.usecase.person.GenerateClientNumberUseCase;
import com.bancopago.backend.application.usecase.person.rulesvalidator.rules.IsClientRule;
import com.bancopago.backend.domain.enums.PersonType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class GenerateClientNumberUseCaseImpl implements GenerateClientNumberUseCase {

    private final IsClientRule isClientRule;

    public GenerateClientNumberUseCaseImpl(IsClientRule isClientRule) {
        this.isClientRule = isClientRule;
    }

    @Override
    public Mono<String> execute(PersonType personType) {
        return isClientRule.validate(personType)
                .then(Mono.fromSupplier(
                        () -> "CLI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                ));
    }
}
