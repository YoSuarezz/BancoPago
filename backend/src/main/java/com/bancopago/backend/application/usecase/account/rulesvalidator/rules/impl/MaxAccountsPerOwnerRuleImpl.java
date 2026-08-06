package com.bancopago.backend.application.usecase.account.rulesvalidator.rules.impl;

import com.bancopago.backend.application.secondaryports.repository.AccountRepository;
import com.bancopago.backend.application.usecase.account.rulesvalidator.rules.MaxAccountsPerOwnerRule;
import com.bancopago.backend.domain.account.exceptions.MaxAccountsExceededException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class MaxAccountsPerOwnerRuleImpl implements MaxAccountsPerOwnerRule {

    public static final int MAX_ACCOUNTS_PER_OWNER = 5;

    private final AccountRepository accountRepository;

    public MaxAccountsPerOwnerRuleImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Mono<Void> validate(UUID ownerId) {
        return accountRepository.findAccountsByOwnerId(ownerId)
                .count()
                .flatMap(count -> {
                    if (count >= MAX_ACCOUNTS_PER_OWNER) {
                        return Mono.error(MaxAccountsExceededException.create(
                                ownerId, MAX_ACCOUNTS_PER_OWNER));
                    }
                    return Mono.empty();
                });
    }
}
