package com.bancopago.backend.application.primaryports.interactor.account.impl;

import com.bancopago.backend.application.primaryports.dto.account.request.ChangeAccountStatusRequest;
import com.bancopago.backend.application.primaryports.dto.account.response.ChangeAccountStatusResponse;
import com.bancopago.backend.application.primaryports.interactor.account.ChangeAccountStatusInteractor;
import com.bancopago.backend.application.primaryports.mapper.account.AccountDTOMapper;
import com.bancopago.backend.application.usecase.account.ChangeAccountStatusCommand;
import com.bancopago.backend.application.usecase.account.ChangeAccountStatusUseCase;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class ChangeAccountStatusInteractorImpl implements ChangeAccountStatusInteractor {

    private final ChangeAccountStatusUseCase changeAccountStatusUseCase;
    private final AccountDTOMapper accountDTOMapper;

    public ChangeAccountStatusInteractorImpl(ChangeAccountStatusUseCase changeAccountStatusUseCase,
                                             AccountDTOMapper accountDTOMapper) {
        this.changeAccountStatusUseCase = changeAccountStatusUseCase;
        this.accountDTOMapper = accountDTOMapper;
    }

    @Override
    public Mono<ChangeAccountStatusResponse> changeAccountStatus(UUID accountId,
                                                                 ChangeAccountStatusRequest request) {
        var command = new ChangeAccountStatusCommand(accountId, request.operation());
        return changeAccountStatusUseCase.changeAccountStatus(command)
                .map(accountDTOMapper::toChangeAccountStatusResponse);
    }
}
