package com.bancopago.backend.application.usecase.transfer.rulesvalidator.rules.impl;

import com.bancopago.backend.application.secondaryports.repository.AccountRepository;
import com.bancopago.backend.application.usecase.transfer.rulesvalidator.rules.SufficientBalanceRule;
import com.bancopago.backend.domain.transfer.TransferDomain;
import com.bancopago.backend.domain.transfer.TransferError;
import com.bancopago.backend.domain.transfer.exceptions.InvalidTransferException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class SufficientBalanceRuleImpl implements SufficientBalanceRule {

    private final AccountRepository accountRepository;

    public SufficientBalanceRuleImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Mono<Void> validate(TransferDomain transfer) {
        return accountRepository.findAccountByNumber(transfer.getSourceAccountNumber())
                .switchIfEmpty(Mono.error(InvalidTransferException.create(TransferError.SOURCE_NOT_FOUND, transfer.getSourceAccountNumber())))
                .flatMap(account -> {
                    if (account.getBalance().compareTo(transfer.getAmount()) < 0) {
                        return Mono.error(InvalidTransferException.create(TransferError.INSUFFICIENT_BALANCE, transfer.getSourceAccountNumber()));
                    }
                    return Mono.empty();
                });
    }
}
