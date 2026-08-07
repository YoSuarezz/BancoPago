package com.bancopago.backend.application.primaryports.interactor.auth.impl;

import com.bancopago.backend.application.primaryports.dto.auth.request.RegisterRequest;
import com.bancopago.backend.application.primaryports.dto.auth.response.AuthResponse;
import com.bancopago.backend.application.primaryports.interactor.auth.RegisterInteractor;
import com.bancopago.backend.application.primaryports.mapper.auth.AuthDTOMapper;
import com.bancopago.backend.application.secondaryports.service.TokenService;
import com.bancopago.backend.application.usecase.auth.RegisterUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RegisterInteractorImpl implements RegisterInteractor {

    private final RegisterUseCase registerUseCase;
    private final TokenService tokenService;
    private final AuthDTOMapper authDTOMapper;

    public RegisterInteractorImpl(RegisterUseCase registerUseCase,
                                   TokenService tokenService,
                                   AuthDTOMapper authDTOMapper) {
        this.registerUseCase = registerUseCase;
        this.tokenService = tokenService;
        this.authDTOMapper = authDTOMapper;
    }

    @Override
    public Mono<AuthResponse> execute(RegisterRequest request) {
        var user = authDTOMapper.toUserDomain(request);
        return registerUseCase.execute(user)
                .map(saved -> authDTOMapper.toAuthResponse(saved, tokenService.generateToken(saved)));
    }
}
