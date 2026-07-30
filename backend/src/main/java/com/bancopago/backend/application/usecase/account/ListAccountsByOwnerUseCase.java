package com.bancopago.backend.application.usecase.account;

import com.bancopago.backend.application.usecase.UseCaseWithReturn;
import com.bancopago.backend.domain.account.AccountDomain;

import java.util.List;
import java.util.UUID;

public interface ListAccountsByOwnerUseCase extends UseCaseWithReturn<UUID, List<AccountDomain>> {
}
