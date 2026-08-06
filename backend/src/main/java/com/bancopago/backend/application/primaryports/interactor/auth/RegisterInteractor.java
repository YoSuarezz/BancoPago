package com.bancopago.backend.application.primaryports.interactor.auth;

import com.bancopago.backend.application.primaryports.dto.auth.request.RegisterRequest;
import com.bancopago.backend.application.primaryports.dto.auth.response.AuthResponse;
import com.bancopago.backend.application.primaryports.interactor.InteractorWithReturn;

public interface RegisterInteractor extends InteractorWithReturn<RegisterRequest, AuthResponse> {}
