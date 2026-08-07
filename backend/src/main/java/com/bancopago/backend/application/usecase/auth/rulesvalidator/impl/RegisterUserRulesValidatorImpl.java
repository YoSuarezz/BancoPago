package com.bancopago.backend.application.usecase.auth.rulesvalidator.impl;

import com.bancopago.backend.application.usecase.auth.rulesvalidator.RegisterUserRulesValidator;
import com.bancopago.backend.application.usecase.auth.rulesvalidator.rules.UniqueUserEmailRule;
import com.bancopago.backend.domain.auth.UserDomain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RegisterUserRulesValidatorImpl implements RegisterUserRulesValidator {

    private final UniqueUserEmailRule uniqueUserEmailRule;

    public RegisterUserRulesValidatorImpl(UniqueUserEmailRule uniqueUserEmailRule) {
        this.uniqueUserEmailRule = uniqueUserEmailRule;
    }

    @Override
    public Mono<Void> validate(UserDomain user) {
        return uniqueUserEmailRule.validate(user.getEmail());
    }
}
