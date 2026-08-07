package com.bancopago.backend.application.usecase.auth.impl;

import com.bancopago.backend.application.secondaryports.repository.UserRepository;
import com.bancopago.backend.application.usecase.auth.RegisterUseCase;
import com.bancopago.backend.application.usecase.auth.rulesvalidator.RegisterUserRulesValidator;
import com.bancopago.backend.domain.auth.UserDomain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
public class RegisterUseCaseImpl implements RegisterUseCase {

    private final UserRepository userRepository;
    private final RegisterUserRulesValidator rulesValidator;
    private final PasswordEncoder passwordEncoder;

    public RegisterUseCaseImpl(UserRepository userRepository,
                                RegisterUserRulesValidator rulesValidator,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.rulesValidator = rulesValidator;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Mono<UserDomain> execute(UserDomain user) {
        return rulesValidator.validate(user)
                .then(Mono.defer(() -> {
                    var hashed = UserDomain.create(
                            user.getEmail(),
                            passwordEncoder.encode(user.getPasswordHash()),
                            user.getRole(),
                            user.getPersonId()
                    );
                    return userRepository.saveUser(hashed);
                }));
    }
}
