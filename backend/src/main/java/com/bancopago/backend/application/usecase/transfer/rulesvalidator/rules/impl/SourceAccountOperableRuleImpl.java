package com.bancopago.backend.application.usecase.transfer.rulesvalidator.rules.impl;

import com.bancopago.backend.application.secondaryports.repository.AccountRepository;
import com.bancopago.backend.application.usecase.transfer.rulesvalidator.rules.SourceAccountOperableRule;
import com.bancopago.backend.domain.enums.AccountStatus;
import com.bancopago.backend.domain.transfer.TransferError;
import com.bancopago.backend.domain.transfer.exceptions.InvalidTransferException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SourceAccountOperableRuleImpl implements SourceAccountOperableRule {

    private final AccountRepository accountRepository;

    public SourceAccountOperableRuleImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Mono<Void> validate(String accountNumber) {
        return accountRepository.findAccountByNumber(accountNumber)
                .switchIfEmpty(Mono.error(InvalidTransferException.create(TransferError.SOURCE_NOT_FOUND, accountNumber)))
                .flatMap(account -> {
                    if (account.getStatus() != AccountStatus.ACTIVE) {
                        return Mono.error(InvalidTransferException.create(
                                TransferError.SOURCE_NOT_OPERABLE, accountNumber));
                    }
                    return Mono.empty();
                });
    }
}
