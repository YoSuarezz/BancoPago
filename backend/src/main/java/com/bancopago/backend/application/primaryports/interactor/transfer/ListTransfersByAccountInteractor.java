package com.bancopago.backend.application.primaryports.interactor.transfer;

import com.bancopago.backend.application.primaryports.dto.transfer.response.ListTransferResponse;
import com.bancopago.backend.application.primaryports.interactor.InteractorWithReturn;

import java.util.List;

public interface ListTransfersByAccountInteractor extends InteractorWithReturn<String, List<ListTransferResponse>> {
}
