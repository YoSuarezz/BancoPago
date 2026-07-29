package com.bancopago.backend.application.primaryports.interactor.account;

import com.bancopago.backend.application.primaryports.dto.account.response.GetAccountBalanceResponse;
import com.bancopago.backend.application.primaryports.interactor.InteractorWithReturn;

import java.util.UUID;

public interface GetAccountBalanceInteractor
        extends InteractorWithReturn<UUID, GetAccountBalanceResponse> {
}
