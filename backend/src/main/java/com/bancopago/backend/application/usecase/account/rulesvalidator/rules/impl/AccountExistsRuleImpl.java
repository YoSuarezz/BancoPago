package com.bancopago.backend.application.usecase.account.rulesvalidator.rules.impl;

import com.bancopago.backend.application.secondaryports.repository.AccountRepository;
import com.bancopago.backend.application.usecase.account.rulesvalidator.rules.AccountExistsRule;
import com.bancopago.backend.domain.account.exceptions.AccountNotFoundException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class AccountExistsRuleImpl implements AccountExistsRule {

    private final AccountRepository accountRepository;

    public AccountExistsRuleImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Mono<Void> validate(UUID accountId) {
        return accountRepository.findAccountById(accountId)
                .switchIfEmpty(Mono.error(AccountNotFoundException.create(accountId)))
                .then();
    }
}
