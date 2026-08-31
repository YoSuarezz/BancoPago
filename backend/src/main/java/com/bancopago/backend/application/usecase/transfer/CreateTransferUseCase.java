package com.bancopago.backend.application.usecase.transfer;

import com.bancopago.backend.application.usecase.UseCaseWithReturn;
import com.bancopago.backend.domain.transfer.TransferDomain;

public interface CreateTransferUseCase extends UseCaseWithReturn<TransferCommand, TransferDomain> {
}
