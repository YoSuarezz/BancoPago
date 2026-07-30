package com.bancopago.backend.application.primaryports.interactor.account;

import com.bancopago.backend.application.primaryports.dto.account.response.GetAccountBalanceResponse;
import com.bancopago.backend.application.primaryports.interactor.InteractorWithFluxReturn;

import java.util.UUID;

public interface StreamAccountBalanceInteractor
        extends InteractorWithFluxReturn<UUID, GetAccountBalanceResponse> {
}
