package com.bancopago.backend.application.primaryports.interactor.account.impl;

import com.bancopago.backend.application.primaryports.dto.account.response.GetAccountBalanceResponse;
import com.bancopago.backend.application.primaryports.interactor.account.StreamAccountBalanceInteractor;
import com.bancopago.backend.application.primaryports.mapper.account.AccountDTOMapper;
import com.bancopago.backend.application.usecase.account.StreamAccountBalanceUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Service
public class StreamAccountBalanceInteractorImpl implements StreamAccountBalanceInteractor {

    private final StreamAccountBalanceUseCase streamAccountBalanceUseCase;
    private final AccountDTOMapper accountDTOMapper;

    public StreamAccountBalanceInteractorImpl(StreamAccountBalanceUseCase streamAccountBalanceUseCase,
                                              AccountDTOMapper accountDTOMapper) {
        this.streamAccountBalanceUseCase = streamAccountBalanceUseCase;
        this.accountDTOMapper = accountDTOMapper;
    }

    @Override
    public Flux<GetAccountBalanceResponse> execute(UUID accountId) {
        return streamAccountBalanceUseCase.execute(accountId)
                .map(accountDTOMapper::toGetAccountBalanceResponse);
    }
}
