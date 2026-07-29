package com.bancopago.backend.application.usecase.account;

import com.bancopago.backend.application.usecase.UseCaseWithReturn;
import com.bancopago.backend.domain.account.AccountDomain;

import java.util.UUID;

public interface UnblockAccountUseCase extends UseCaseWithReturn<UUID, AccountDomain> {
}
