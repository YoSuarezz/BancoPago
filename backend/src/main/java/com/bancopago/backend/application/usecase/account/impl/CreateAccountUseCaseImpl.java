package com.bancopago.backend.application.usecase.account.impl;

import com.bancopago.backend.application.secondaryports.repository.AccountRepository;
import com.bancopago.backend.application.usecase.account.AccountNumberGenerator;
import com.bancopago.backend.application.usecase.account.CreateAccountCommand;
import com.bancopago.backend.application.usecase.account.CreateAccountUseCase;
import com.bancopago.backend.application.usecase.account.rulesvalidator.CreateAccountRulesValidator;
import com.bancopago.backend.domain.account.AccountDomain;
import com.bancopago.backend.domain.account.vo.AccountNumber;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
public class CreateAccountUseCaseImpl implements CreateAccountUseCase {

    private final AccountRepository accountRepository;
    private final CreateAccountRulesValidator rulesValidator;
    private final AccountNumberGenerator accountNumberGenerator;

    public CreateAccountUseCaseImpl(AccountRepository accountRepository,
                                    CreateAccountRulesValidator rulesValidator,
                                    AccountNumberGenerator accountNumberGenerator) {
        this.accountRepository = accountRepository;
        this.rulesValidator = rulesValidator;
        this.accountNumberGenerator = accountNumberGenerator;
    }

    @Override
    @Transactional
    public Mono<AccountDomain> execute(CreateAccountCommand command) {
        return accountNumberGenerator.generateUniqueAccountNumber()
                .map(number -> new AccountDomain(command.ownerId(), number, command.type()))
                .flatMap(account -> rulesValidator.validate(account).thenReturn(account))
                .flatMap(accountRepository::saveAccount);
    }
}
