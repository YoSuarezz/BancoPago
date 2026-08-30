package com.bancopago.backend.application.primaryports.interactor.account;

import com.bancopago.backend.application.primaryports.dto.account.response.ListAccountResponse;
import com.bancopago.backend.application.primaryports.interactor.InteractorWithReturn;

import java.util.List;
import java.util.UUID;

public interface ListAccountsByOwnerInteractor
        extends InteractorWithReturn<UUID, List<ListAccountResponse>> {
}
