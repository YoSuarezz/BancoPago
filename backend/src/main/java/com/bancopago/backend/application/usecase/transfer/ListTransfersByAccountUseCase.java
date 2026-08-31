package com.bancopago.backend.application.usecase.transfer;

import com.bancopago.backend.application.usecase.UseCaseWithFluxReturn;
import com.bancopago.backend.domain.transfer.TransferDomain;

public interface ListTransfersByAccountUseCase extends UseCaseWithFluxReturn<String, TransferDomain> {
}
