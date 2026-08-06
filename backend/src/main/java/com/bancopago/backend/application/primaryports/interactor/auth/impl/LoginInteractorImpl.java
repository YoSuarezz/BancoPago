package com.bancopago.backend.application.primaryports.interactor.auth.impl;

import com.bancopago.backend.application.primaryports.dto.auth.request.LoginRequest;
import com.bancopago.backend.application.primaryports.dto.auth.response.AuthResponse;
import com.bancopago.backend.application.primaryports.interactor.auth.LoginInteractor;
import com.bancopago.backend.application.usecase.auth.LoginUseCase;
import com.bancopago.backend.domain.auth.LoginCredential;
import com.bancopago.backend.infrastructure.secondaryadapters.jwt.JwtService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class LoginInteractorImpl implements LoginInteractor {

    private final LoginUseCase loginUseCase;
    private final JwtService jwtService;

    public LoginInteractorImpl(LoginUseCase loginUseCase, JwtService jwtService) {
        this.loginUseCase = loginUseCase;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<AuthResponse> execute(LoginRequest request) {
        var credential = new LoginCredential(request.email(), request.password());
        return loginUseCase.execute(credential)
                .map(user -> AuthResponse.of(jwtService.generateToken(user), user.getEmail(), user.getRole()));
    }
}
