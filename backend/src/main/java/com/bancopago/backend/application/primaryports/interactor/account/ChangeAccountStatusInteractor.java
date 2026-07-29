package com.bancopago.backend.application.primaryports.interactor.account;

import com.bancopago.backend.application.primaryports.dto.account.request.ChangeAccountStatusRequest;
import com.bancopago.backend.application.primaryports.dto.account.response.ChangeAccountStatusResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ChangeAccountStatusInteractor {

    Mono<ChangeAccountStatusResponse> changeAccountStatus(UUID accountId, ChangeAccountStatusRequest request);
}
