package com.bancopago.backend.application.primaryports.interactor.account.impl;

import com.bancopago.backend.application.primaryports.dto.account.request.CreateAccountRequest;
import com.bancopago.backend.application.primaryports.dto.account.response.CreateAccountResponse;
import com.bancopago.backend.application.primaryports.interactor.account.CreateAccountInteractor;
import com.bancopago.backend.application.primaryports.mapper.account.AccountDTOMapper;
import com.bancopago.backend.application.usecase.account.CreateAccountCommand;
import com.bancopago.backend.application.usecase.account.CreateAccountUseCase;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * DTO → Command de aplicación → UseCase → Domain → Response.
 * No construye AccountDomain aquí: el número se genera en el UseCase.
 */
@Component
public class CreateAccountInteractorImpl implements CreateAccountInteractor {

    private final CreateAccountUseCase createAccountUseCase;
    private final AccountDTOMapper accountDTOMapper;

    public CreateAccountInteractorImpl(CreateAccountUseCase createAccountUseCase,
                                       AccountDTOMapper accountDTOMapper) {
        this.createAccountUseCase = createAccountUseCase;
        this.accountDTOMapper = accountDTOMapper;
    }

    @Override
    public Mono<CreateAccountResponse> createAccount(CreateAccountRequest request) {
        var command = new CreateAccountCommand(request.ownerId(), request.type());
        return createAccountUseCase.createAccount(command)
                .map(accountDTOMapper::toCreateAccountResponse);
    }
}
