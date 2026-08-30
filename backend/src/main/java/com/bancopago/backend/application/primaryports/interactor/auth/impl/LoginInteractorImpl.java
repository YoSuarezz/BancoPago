package com.bancopago.backend.application.primaryports.interactor.auth.impl;

import com.bancopago.backend.application.primaryports.dto.auth.request.LoginRequest;
import com.bancopago.backend.application.primaryports.dto.auth.response.AuthResponse;
import com.bancopago.backend.application.primaryports.interactor.auth.LoginInteractor;
import com.bancopago.backend.application.primaryports.mapper.auth.AuthDTOMapper;
import com.bancopago.backend.application.secondaryports.service.TokenService;
import com.bancopago.backend.application.usecase.auth.LoginUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class LoginInteractorImpl implements LoginInteractor {

    private final LoginUseCase loginUseCase;
    private final TokenService tokenService;
    private final AuthDTOMapper authDTOMapper;

    public LoginInteractorImpl(LoginUseCase loginUseCase,
                                TokenService tokenService,
                                AuthDTOMapper authDTOMapper) {
        this.loginUseCase = loginUseCase;
        this.tokenService = tokenService;
        this.authDTOMapper = authDTOMapper;
    }

    @Override
    public Mono<AuthResponse> execute(LoginRequest request) {
        var credential = authDTOMapper.toLoginCredential(request);
        return loginUseCase.execute(credential)
                .map(user -> authDTOMapper.toAuthResponse(user, tokenService.generateToken(user)));
    }
}
