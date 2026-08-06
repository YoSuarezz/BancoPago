package com.bancopago.backend.application.primaryports.interactor.auth.impl;

import com.bancopago.backend.application.primaryports.dto.auth.request.RegisterRequest;
import com.bancopago.backend.application.primaryports.dto.auth.response.AuthResponse;
import com.bancopago.backend.application.primaryports.interactor.auth.RegisterInteractor;
import com.bancopago.backend.application.usecase.auth.RegisterUseCase;
import com.bancopago.backend.domain.auth.UserDomain;
import com.bancopago.backend.infrastructure.secondaryadapters.jwt.JwtService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RegisterInteractorImpl implements RegisterInteractor {

    private final RegisterUseCase registerUseCase;
    private final JwtService jwtService;

    public RegisterInteractorImpl(RegisterUseCase registerUseCase, JwtService jwtService) {
        this.registerUseCase = registerUseCase;
        this.jwtService = jwtService;
    }

    @Override
    public Mono<AuthResponse> execute(RegisterRequest request) {
        UserDomain newUser = UserDomain.create(
                request.email(),
                request.password(),
                request.role(),
                null
        );
        return registerUseCase.execute(newUser)
                .map(saved -> AuthResponse.of(jwtService.generateToken(saved), saved.getEmail(), saved.getRole()));
    }
}
