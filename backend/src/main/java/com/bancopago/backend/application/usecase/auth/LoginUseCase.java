package com.bancopago.backend.application.usecase.auth;

import com.bancopago.backend.application.usecase.UseCaseWithReturn;
import com.bancopago.backend.domain.auth.LoginCredential;
import com.bancopago.backend.domain.auth.UserDomain;

public interface LoginUseCase extends UseCaseWithReturn<LoginCredential, UserDomain> {}
