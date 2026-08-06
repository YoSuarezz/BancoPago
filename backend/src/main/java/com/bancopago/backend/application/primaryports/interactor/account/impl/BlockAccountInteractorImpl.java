package com.bancopago.backend.application.primaryports.interactor.account.impl;

import com.bancopago.backend.application.primaryports.dto.account.response.AccountStatusResponse;
import com.bancopago.backend.application.primaryports.interactor.account.BlockAccountInteractor;
import com.bancopago.backend.application.primaryports.mapper.account.AccountDTOMapper;
import com.bancopago.backend.application.usecase.account.BlockAccountUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class BlockAccountInteractorImpl implements BlockAccountInteractor {

    private final BlockAccountUseCase blockAccountUseCase;
    private final AccountDTOMapper accountDTOMapper;

    public BlockAccountInteractorImpl(BlockAccountUseCase blockAccountUseCase,
                                      AccountDTOMapper accountDTOMapper) {
        this.blockAccountUseCase = blockAccountUseCase;
        this.accountDTOMapper = accountDTOMapper;
    }

    @Override
    public Mono<AccountStatusResponse> execute(UUID accountId) {
        return blockAccountUseCase.execute(accountId)
                .map(accountDTOMapper::toAccountStatusResponse);
    }
}
