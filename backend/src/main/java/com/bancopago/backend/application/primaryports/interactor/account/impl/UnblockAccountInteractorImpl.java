package com.bancopago.backend.application.primaryports.interactor.account.impl;

import com.bancopago.backend.application.primaryports.dto.account.response.AccountStatusResponse;
import com.bancopago.backend.application.primaryports.interactor.account.UnblockAccountInteractor;
import com.bancopago.backend.application.primaryports.mapper.account.AccountDTOMapper;
import com.bancopago.backend.application.usecase.account.UnblockAccountUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class UnblockAccountInteractorImpl implements UnblockAccountInteractor {

    private final UnblockAccountUseCase unblockAccountUseCase;
    private final AccountDTOMapper accountDTOMapper;

    public UnblockAccountInteractorImpl(UnblockAccountUseCase unblockAccountUseCase,
                                        AccountDTOMapper accountDTOMapper) {
        this.unblockAccountUseCase = unblockAccountUseCase;
        this.accountDTOMapper = accountDTOMapper;
    }

    @Override
    public Mono<AccountStatusResponse> execute(UUID accountId) {
        return unblockAccountUseCase.execute(accountId)
                .map(accountDTOMapper::toAccountStatusResponse);
    }
}
