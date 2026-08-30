package com.bancopago.backend.application.usecase.account;

import com.bancopago.backend.application.usecase.UseCaseWithReturn;
import com.bancopago.backend.domain.account.AccountDomain;

public interface CreateAccountUseCase extends UseCaseWithReturn<CreateAccountCommand, AccountDomain> {
}
