package com.bancopago.backend.application.primaryports.interactor.account.impl;

import com.bancopago.backend.application.primaryports.dto.account.response.GetAccountBalanceResponse;
import com.bancopago.backend.application.primaryports.interactor.account.GetAccountBalanceInteractor;
import com.bancopago.backend.application.primaryports.mapper.account.AccountDTOMapper;
import com.bancopago.backend.application.usecase.account.GetAccountBalanceUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class GetAccountBalanceInteractorImpl implements GetAccountBalanceInteractor {

    private final GetAccountBalanceUseCase getAccountBalanceUseCase;
    private final AccountDTOMapper accountDTOMapper;

    public GetAccountBalanceInteractorImpl(GetAccountBalanceUseCase getAccountBalanceUseCase,
                                           AccountDTOMapper accountDTOMapper) {
        this.getAccountBalanceUseCase = getAccountBalanceUseCase;
        this.accountDTOMapper = accountDTOMapper;
    }

    @Override
    public Mono<GetAccountBalanceResponse> execute(UUID accountId) {
        return getAccountBalanceUseCase.execute(accountId)
                .map(accountDTOMapper::toGetAccountBalanceResponse);
    }
}
