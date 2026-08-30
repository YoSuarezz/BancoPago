package com.bancopago.backend.application.primaryports.interactor.auth;

import com.bancopago.backend.application.primaryports.dto.auth.request.LoginRequest;
import com.bancopago.backend.application.primaryports.dto.auth.response.AuthResponse;
import com.bancopago.backend.application.primaryports.interactor.InteractorWithReturn;

public interface LoginInteractor extends InteractorWithReturn<LoginRequest, AuthResponse> {}
