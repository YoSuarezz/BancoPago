package com.bancopago.backend.application.usecase.account.impl;

import com.bancopago.backend.application.secondaryports.repository.AccountRepository;
import com.bancopago.backend.application.usecase.account.GetAccountBalanceUseCase;
import com.bancopago.backend.domain.account.AccountDomain;
import com.bancopago.backend.domain.account.exceptions.AccountNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class GetAccountBalanceUseCaseImpl implements GetAccountBalanceUseCase {

    private final AccountRepository accountRepository;

    public GetAccountBalanceUseCaseImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Mono<AccountDomain> getAccountBalance(UUID accountId) {
        return accountRepository.findAccountById(accountId)
                .switchIfEmpty(Mono.error(AccountNotFoundException.create(accountId)));
    }
}
