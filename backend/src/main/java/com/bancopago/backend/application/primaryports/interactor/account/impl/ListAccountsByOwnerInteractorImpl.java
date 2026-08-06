package com.bancopago.backend.application.primaryports.interactor.account.impl;

import com.bancopago.backend.application.primaryports.dto.account.response.ListAccountResponse;
import com.bancopago.backend.application.primaryports.interactor.account.ListAccountsByOwnerInteractor;
import com.bancopago.backend.application.primaryports.mapper.account.AccountDTOMapper;
import com.bancopago.backend.application.usecase.account.ListAccountsByOwnerUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class ListAccountsByOwnerInteractorImpl implements ListAccountsByOwnerInteractor {

    private final ListAccountsByOwnerUseCase listAccountsByOwnerUseCase;
    private final AccountDTOMapper accountDTOMapper;

    public ListAccountsByOwnerInteractorImpl(ListAccountsByOwnerUseCase listAccountsByOwnerUseCase,
                                             AccountDTOMapper accountDTOMapper) {
        this.listAccountsByOwnerUseCase = listAccountsByOwnerUseCase;
        this.accountDTOMapper = accountDTOMapper;
    }

    @Override
    public Mono<List<ListAccountResponse>> execute(UUID ownerId) {
        return listAccountsByOwnerUseCase.execute(ownerId)
                .map(accounts -> accounts.stream()
                        .map(accountDTOMapper::toListAccountResponse)
                        .toList());
    }
}
