package com.bancopago.backend.application.primaryports.interactor.account.impl;

import com.bancopago.backend.application.primaryports.dto.account.request.CreateAccountRequest;
import com.bancopago.backend.application.primaryports.dto.account.response.CreateAccountResponse;
import com.bancopago.backend.application.primaryports.interactor.account.CreateAccountInteractor;
import com.bancopago.backend.application.primaryports.mapper.account.AccountDTOMapper;
import com.bancopago.backend.application.usecase.account.AccountNumberGenerator;
import com.bancopago.backend.application.usecase.account.CreateAccountUseCase;
import com.bancopago.backend.domain.account.AccountDomain;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CreateAccountInteractorImpl implements CreateAccountInteractor {

    private final CreateAccountUseCase createAccountUseCase;
    private final AccountNumberGenerator accountNumberGenerator;
    private final AccountDTOMapper accountDTOMapper;

    public CreateAccountInteractorImpl(CreateAccountUseCase createAccountUseCase,
                                       AccountNumberGenerator accountNumberGenerator,
                                       AccountDTOMapper accountDTOMapper) {
        this.createAccountUseCase = createAccountUseCase;
        this.accountNumberGenerator = accountNumberGenerator;
        this.accountDTOMapper = accountDTOMapper;
    }

    @Override
    public Mono<CreateAccountResponse> execute(CreateAccountRequest request) {
        return accountNumberGenerator.generateUniqueAccountNumber()
                .map(number -> new AccountDomain(request.ownerId(), number, request.type()))
                .flatMap(createAccountUseCase::execute)
                .map(accountDTOMapper::toCreateAccountResponse);
    }
}
