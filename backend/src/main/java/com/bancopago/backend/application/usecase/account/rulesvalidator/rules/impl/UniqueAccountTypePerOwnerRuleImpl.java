package com.bancopago.backend.application.usecase.account.rulesvalidator.rules.impl;

import com.bancopago.backend.application.secondaryports.repository.AccountRepository;
import com.bancopago.backend.application.usecase.account.rulesvalidator.rules.UniqueAccountTypePerOwnerRule;
import com.bancopago.backend.domain.account.AccountDomain;
import com.bancopago.backend.domain.account.exceptions.DuplicateAccountTypeException;
import com.bancopago.backend.domain.enums.AccountStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class UniqueAccountTypePerOwnerRuleImpl implements UniqueAccountTypePerOwnerRule {

    private final AccountRepository accountRepository;

    public UniqueAccountTypePerOwnerRuleImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Mono<Void> validate(AccountDomain account) {
        return accountRepository.findAccountsByOwnerId(account.getOwnerId())
                .filter(existing -> existing.getType() == account.getType())
                .filter(existing -> existing.getStatus() != AccountStatus.INACTIVE)
                .hasElements()
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(DuplicateAccountTypeException.create(
                                account.getOwnerId(), account.getType()));
                    }
                    return Mono.empty();
                });
    }
}
