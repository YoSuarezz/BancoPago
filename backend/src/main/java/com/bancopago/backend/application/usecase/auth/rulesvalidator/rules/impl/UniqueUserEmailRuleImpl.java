package com.bancopago.backend.application.usecase.auth.rulesvalidator.rules.impl;

import com.bancopago.backend.application.secondaryports.repository.UserRepository;
import com.bancopago.backend.application.usecase.auth.rulesvalidator.rules.UniqueUserEmailRule;
import com.bancopago.backend.domain.auth.exceptions.DuplicateUserEmailException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UniqueUserEmailRuleImpl implements UniqueUserEmailRule {

    private final UserRepository userRepository;

    public UniqueUserEmailRuleImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<Void> validate(String email) {
        return userRepository.existsByEmail(email)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(DuplicateUserEmailException.create(email));
                    }
                    return Mono.empty();
                });
    }
}
