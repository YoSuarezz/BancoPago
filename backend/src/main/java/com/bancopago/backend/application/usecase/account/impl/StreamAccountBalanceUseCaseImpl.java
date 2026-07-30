package com.bancopago.backend.application.usecase.account.impl;

import com.bancopago.backend.application.secondaryports.repository.AccountRepository;
import com.bancopago.backend.application.usecase.account.StreamAccountBalanceUseCase;
import com.bancopago.backend.application.usecase.account.rulesvalidator.StreamAccountBalanceRulesValidator;
import com.bancopago.backend.domain.account.AccountDomain;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.UUID;

@Service
public class StreamAccountBalanceUseCaseImpl implements StreamAccountBalanceUseCase {

    static final Duration POLL_INTERVAL = Duration.ofSeconds(5);

    private final AccountRepository accountRepository;
    private final StreamAccountBalanceRulesValidator rulesValidator;

    public StreamAccountBalanceUseCaseImpl(AccountRepository accountRepository,
                                           StreamAccountBalanceRulesValidator rulesValidator) {
        this.accountRepository = accountRepository;
        this.rulesValidator = rulesValidator;
    }

    @Override
    public Flux<AccountDomain> execute(UUID accountId) {
        // Módulo 1 (polling): si la cuenta desaparece después de la validación inicial,
        // findAccountById puede devolver Mono.empty() en un tick y ese ciclo no emite evento.
        // El stream sigue abierto y reintenta en el siguiente intervalo.
        // En Módulo 2 este comportamiento será reemplazado por publicación/suscripción.
        return rulesValidator.validate(accountId)
                .thenMany(Flux.interval(Duration.ZERO, POLL_INTERVAL)
                        .concatMap(tick -> accountRepository.findAccountById(accountId)));
    }
}
