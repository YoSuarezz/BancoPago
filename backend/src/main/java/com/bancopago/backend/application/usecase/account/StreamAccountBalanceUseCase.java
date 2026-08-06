package com.bancopago.backend.application.usecase.account;

import com.bancopago.backend.application.usecase.UseCaseWithFluxReturn;
import com.bancopago.backend.domain.account.AccountDomain;

import java.util.UUID;

public interface StreamAccountBalanceUseCase extends UseCaseWithFluxReturn<UUID, AccountDomain> {
}
