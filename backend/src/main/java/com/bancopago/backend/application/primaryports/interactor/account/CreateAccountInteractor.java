package com.bancopago.backend.application.primaryports.interactor.account;

import com.bancopago.backend.application.primaryports.dto.account.request.CreateAccountRequest;
import com.bancopago.backend.application.primaryports.dto.account.response.CreateAccountResponse;
import reactor.core.publisher.Mono;

public interface CreateAccountInteractor {

    Mono<CreateAccountResponse> createAccount(CreateAccountRequest request);
}
