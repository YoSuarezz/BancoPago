package com.bancopago.backend.application.primaryports.interactor.account;

import com.bancopago.backend.application.primaryports.dto.account.request.CreateAccountRequest;
import com.bancopago.backend.application.primaryports.dto.account.response.CreateAccountResponse;
import com.bancopago.backend.application.primaryports.interactor.InteractorWithReturn;

public interface CreateAccountInteractor
        extends InteractorWithReturn<CreateAccountRequest, CreateAccountResponse> {
}
