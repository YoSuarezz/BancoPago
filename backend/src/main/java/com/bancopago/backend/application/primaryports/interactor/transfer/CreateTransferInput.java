package com.bancopago.backend.application.primaryports.interactor.transfer;

import com.bancopago.backend.application.primaryports.dto.transfer.request.CreateTransferRequest;

public record CreateTransferInput(CreateTransferRequest request, String idempotencyKey) {
}
