package com.bancopago.backend.application.primaryports.interactor.transfer;

import com.bancopago.backend.application.primaryports.dto.transfer.response.GetTransferResponse;
import com.bancopago.backend.application.primaryports.interactor.InteractorWithReturn;

import java.util.UUID;

public interface GetTransferByIdInteractor extends InteractorWithReturn<UUID, GetTransferResponse> {
}
