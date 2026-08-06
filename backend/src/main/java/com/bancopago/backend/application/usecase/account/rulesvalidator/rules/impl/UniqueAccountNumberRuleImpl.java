package com.bancopago.backend.application.usecase.account.rulesvalidator.rules.impl;

import com.bancopago.backend.application.secondaryports.repository.AccountRepository;
import com.bancopago.backend.application.usecase.account.rulesvalidator.rules.UniqueAccountNumberRule;
import com.bancopago.backend.domain.account.exceptions.DuplicateAccountException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UniqueAccountNumberRuleImpl implements UniqueAccountNumberRule {

    private final AccountRepository accountRepository;

    public UniqueAccountNumberRuleImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Mono<Void> validate(String accountNumber) {
        return accountRepository.existsAccountByNumber(accountNumber)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(DuplicateAccountException.create(accountNumber));
                    }
                    return Mono.empty();
                });
    }
}
