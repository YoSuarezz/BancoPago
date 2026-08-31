package com.bancopago.backend.application.usecase.transfer.rulesvalidator.rules.impl;

import com.bancopago.backend.application.secondaryports.repository.AccountRepository;
import com.bancopago.backend.application.usecase.transfer.rulesvalidator.rules.SourceAccountExistsRule;
import com.bancopago.backend.domain.transfer.TransferError;
import com.bancopago.backend.domain.transfer.exceptions.InvalidTransferException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SourceAccountExistsRuleImpl implements SourceAccountExistsRule {

    private final AccountRepository accountRepository;

    public SourceAccountExistsRuleImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Mono<Void> validate(String accountNumber) {
        return accountRepository.findAccountByNumber(accountNumber)
                .switchIfEmpty(Mono.error(InvalidTransferException.create(TransferError.SOURCE_NOT_FOUND, accountNumber)))
                .then();
    }
}
