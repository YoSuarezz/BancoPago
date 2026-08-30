package com.bancopago.backend.application.usecase.account.rulesvalidator.impl;

import com.bancopago.backend.application.usecase.account.rulesvalidator.StreamAccountBalanceRulesValidator;
import com.bancopago.backend.application.usecase.account.rulesvalidator.rules.AccountExistsRule;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class StreamAccountBalanceRulesValidatorImpl implements StreamAccountBalanceRulesValidator {

    private final AccountExistsRule accountExistsRule;

    public StreamAccountBalanceRulesValidatorImpl(AccountExistsRule accountExistsRule) {
        this.accountExistsRule = accountExistsRule;
    }

    @Override
    public Mono<Void> validate(UUID accountId) {
        return accountExistsRule.validate(accountId);
    }
}
