package com.bancopago.backend.application.usecase.auth.impl;

import com.bancopago.backend.application.secondaryports.repository.UserRepository;
import com.bancopago.backend.application.usecase.auth.LoginUseCase;
import com.bancopago.backend.domain.auth.LoginCredential;
import com.bancopago.backend.domain.auth.UserDomain;
import com.bancopago.backend.domain.auth.exceptions.InvalidCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class LoginUseCaseImpl implements LoginUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginUseCaseImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Mono<UserDomain> execute(LoginCredential credential) {
        return userRepository.findUserByEmail(credential.email())
                .switchIfEmpty(Mono.error(InvalidCredentialsException.create()))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(credential.rawPassword(), user.getPasswordHash())) {
                        return Mono.error(InvalidCredentialsException.create());
                    }
                    return Mono.just(user);
                });
    }
}
