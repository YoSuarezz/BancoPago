package com.bancopago.backend.application.usecase.auth.impl;

import com.bancopago.backend.application.secondaryports.repository.UserRepository;
import com.bancopago.backend.application.usecase.auth.RegisterUseCase;
import com.bancopago.backend.domain.auth.UserDomain;
import com.bancopago.backend.domain.auth.exceptions.DuplicateUserEmailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
public class RegisterUseCaseImpl implements RegisterUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterUseCaseImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Mono<UserDomain> execute(UserDomain user) {
        return userRepository.existsByEmail(user.getEmail())
                .flatMap(exists -> {
                    if (exists) return Mono.error(DuplicateUserEmailException.create(user.getEmail()));
                    var hashed = UserDomain.create(
                            user.getEmail(),
                            passwordEncoder.encode(user.getPasswordHash()),
                            user.getRole(),
                            user.getPersonId()
                    );
                    return userRepository.saveUser(hashed);
                });
    }
}
