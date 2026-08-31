package com.bancopago.backend.application.usecase.transfer.rulesvalidator.rules.impl;

import com.bancopago.backend.application.secondaryports.repository.AccountRepository;
import com.bancopago.backend.application.usecase.transfer.rulesvalidator.rules.TargetAccountExistsRule;
import com.bancopago.backend.domain.transfer.TransferError;
import com.bancopago.backend.domain.transfer.exceptions.InvalidTransferException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TargetAccountExistsRuleImpl implements TargetAccountExistsRule {

    private final AccountRepository accountRepository;

    public TargetAccountExistsRuleImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Mono<Void> validate(String accountNumber) {
        return accountRepository.findAccountByNumber(accountNumber)
                .switchIfEmpty(Mono.error(InvalidTransferException.create(TransferError.TARGET_NOT_FOUND, accountNumber)))
                .then();
    }
}
