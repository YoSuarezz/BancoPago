package com.bancopago.backend.application.usecase.account.impl;

import com.bancopago.backend.application.secondaryports.repository.AccountRepository;
import com.bancopago.backend.application.usecase.account.AccountNumberGenerator;
import com.bancopago.backend.application.usecase.account.CreateAccountCommand;
import com.bancopago.backend.application.usecase.account.CreateAccountUseCase;
import com.bancopago.backend.application.usecase.account.rulesvalidator.CreateAccountRulesValidator;
import com.bancopago.backend.domain.account.AccountDomain;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CreateAccountUseCaseImpl implements CreateAccountUseCase {

    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final CreateAccountRulesValidator rulesValidator;

    public CreateAccountUseCaseImpl(AccountRepository accountRepository,
                                    AccountNumberGenerator accountNumberGenerator,
                                    CreateAccountRulesValidator rulesValidator) {
        this.accountRepository = accountRepository;
        this.accountNumberGenerator = accountNumberGenerator;
        this.rulesValidator = rulesValidator;
    }

    @Override
    public Mono<AccountDomain> createAccount(CreateAccountCommand command) {
        return Mono.defer(accountNumberGenerator::generateUniqueAccountNumber)
                .map(number -> new AccountDomain(command.ownerId(), number, command.type()))
                .flatMap(account -> rulesValidator.validate(account)
                        .then(Mono.defer(() -> accountRepository.saveAccount(account))));
    }
}
