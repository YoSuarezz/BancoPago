package com.bancopago.backend.application.primaryports.interactor.account.impl;

import com.bancopago.backend.application.primaryports.dto.account.request.CreateAccountRequest;
import com.bancopago.backend.application.primaryports.dto.account.response.CreateAccountResponse;
import com.bancopago.backend.application.primaryports.interactor.account.CreateAccountInteractor;
import com.bancopago.backend.application.primaryports.mapper.account.AccountDTOMapper;
import com.bancopago.backend.application.usecase.account.CreateAccountCommand;
import com.bancopago.backend.application.usecase.account.CreateAccountUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CreateAccountInteractorImpl implements CreateAccountInteractor {

    private final CreateAccountUseCase createAccountUseCase;
    private final AccountDTOMapper accountDTOMapper;

    public CreateAccountInteractorImpl(CreateAccountUseCase createAccountUseCase,
                                       AccountDTOMapper accountDTOMapper) {
        this.createAccountUseCase = createAccountUseCase;
        this.accountDTOMapper = accountDTOMapper;
    }

    @Override
    public Mono<CreateAccountResponse> execute(CreateAccountRequest request) {
        return createAccountUseCase.execute(new CreateAccountCommand(request.ownerId(), request.type()))
                .map(accountDTOMapper::toCreateAccountResponse);
    }
}
