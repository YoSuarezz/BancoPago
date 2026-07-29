package com.bancopago.backend.application.primaryports.interactor.account.impl;

import com.bancopago.backend.application.primaryports.dto.account.response.AccountStatusResponse;
import com.bancopago.backend.application.primaryports.interactor.account.CloseAccountInteractor;
import com.bancopago.backend.application.primaryports.mapper.account.AccountDTOMapper;
import com.bancopago.backend.application.usecase.account.CloseAccountUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class CloseAccountInteractorImpl implements CloseAccountInteractor {

    private final CloseAccountUseCase closeAccountUseCase;
    private final AccountDTOMapper accountDTOMapper;

    public CloseAccountInteractorImpl(CloseAccountUseCase closeAccountUseCase,
                                      AccountDTOMapper accountDTOMapper) {
        this.closeAccountUseCase = closeAccountUseCase;
        this.accountDTOMapper = accountDTOMapper;
    }

    @Override
    public Mono<AccountStatusResponse> execute(UUID accountId) {
        return closeAccountUseCase.execute(accountId)
                .map(accountDTOMapper::toAccountStatusResponse);
    }
}
