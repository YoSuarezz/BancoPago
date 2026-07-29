package com.bancopago.backend.application.usecase.account;

import com.bancopago.backend.application.secondaryports.repository.AccountRepository;
import com.bancopago.backend.domain.account.exceptions.DuplicateAccountException;
import com.bancopago.backend.domain.account.vo.AccountNumber;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;

/**
 * Genera un número de cuenta de 10 dígitos y reintenta si ya existe en BD.
 */
@Component
public class AccountNumberGenerator {

    private static final int ACCOUNT_NUMBER_LENGTH = 10;
    private static final int MAX_ATTEMPTS = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AccountRepository accountRepository;

    public AccountNumberGenerator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Mono<AccountNumber> generateUniqueAccountNumber() {
        return tryGenerate(0);
    }

    private Mono<AccountNumber> tryGenerate(int attempt) {
        if (attempt >= MAX_ATTEMPTS) {
            return Mono.error(DuplicateAccountException.create("generated"));
        }
        String candidate = randomDigits();
        return accountRepository.existsAccountByNumber(candidate)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return tryGenerate(attempt + 1);
                    }
                    return Mono.just(new AccountNumber(candidate));
                });
    }

    private static String randomDigits() {
        StringBuilder builder = new StringBuilder(AccountNumberGenerator.ACCOUNT_NUMBER_LENGTH);
        for (int i = 0; i < AccountNumberGenerator.ACCOUNT_NUMBER_LENGTH; i++) {
            builder.append(RANDOM.nextInt(10));
        }
        return builder.toString();
    }
}
