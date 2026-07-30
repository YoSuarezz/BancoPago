package com.bancopago.backend.application.usecase.account.impl;

import com.bancopago.backend.application.secondaryports.repository.AccountRepository;
import com.bancopago.backend.application.usecase.account.ListAccountsByOwnerUseCase;
import com.bancopago.backend.domain.account.AccountDomain;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class ListAccountsByOwnerUseCaseImpl implements ListAccountsByOwnerUseCase {

    private final AccountRepository accountRepository;

    public ListAccountsByOwnerUseCaseImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Mono<List<AccountDomain>> execute(UUID ownerId) {
        return accountRepository.findAccountsByOwnerId(ownerId).collectList();
    }
}
