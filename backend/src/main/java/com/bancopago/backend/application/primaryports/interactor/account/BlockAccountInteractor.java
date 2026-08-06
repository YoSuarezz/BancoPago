package com.bancopago.backend.application.primaryports.interactor.account;

import com.bancopago.backend.application.primaryports.dto.account.response.AccountStatusResponse;
import com.bancopago.backend.application.primaryports.interactor.InteractorWithReturn;

import java.util.UUID;

public interface BlockAccountInteractor extends InteractorWithReturn<UUID, AccountStatusResponse> {
}
