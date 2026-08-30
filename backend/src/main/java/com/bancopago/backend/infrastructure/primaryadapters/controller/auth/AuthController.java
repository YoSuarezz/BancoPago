package com.bancopago.backend.infrastructure.primaryadapters.controller.auth;

import com.bancopago.backend.application.primaryports.dto.auth.request.LoginRequest;
import com.bancopago.backend.application.primaryports.dto.auth.request.RegisterRequest;
import com.bancopago.backend.application.primaryports.dto.auth.response.AuthResponse;
import com.bancopago.backend.application.primaryports.interactor.auth.LoginInteractor;
import com.bancopago.backend.application.primaryports.interactor.auth.RegisterInteractor;
import com.bancopago.backend.infrastructure.primaryadapters.adapter.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterInteractor registerInteractor;
    private final LoginInteractor loginInteractor;

    public AuthController(RegisterInteractor registerInteractor, LoginInteractor loginInteractor) {
        this.registerInteractor = registerInteractor;
        this.loginInteractor = loginInteractor;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return registerInteractor.execute(request)
                .map(auth -> ApiResponse.of(auth, "Usuario registrado exitosamente"));
    }

    @PostMapping("/login")
    public Mono<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        return loginInteractor.execute(request)
                .map(auth -> ApiResponse.of(auth, "Autenticación exitosa"));
    }
}
