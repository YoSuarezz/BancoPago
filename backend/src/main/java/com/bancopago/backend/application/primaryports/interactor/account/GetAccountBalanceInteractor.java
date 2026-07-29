package com.bancopago.backend.application.primaryports.interactor.account;

import com.bancopago.backend.application.primaryports.dto.account.response.GetAccountBalanceResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface GetAccountBalanceInteractor {

    Mono<GetAccountBalanceResponse> getAccountBalance(UUID accountId);
}
