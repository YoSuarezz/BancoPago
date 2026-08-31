package com.bancopago.backend.application.usecase.transfer;

import com.bancopago.backend.application.usecase.UseCaseWithReturn;
import com.bancopago.backend.domain.transfer.TransferDomain;

import java.util.UUID;

public interface GetTransferByIdUseCase extends UseCaseWithReturn<UUID, TransferDomain> {
}
