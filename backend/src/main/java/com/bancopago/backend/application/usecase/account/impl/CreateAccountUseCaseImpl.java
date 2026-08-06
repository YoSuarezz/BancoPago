package com.bancopago.backend.application.usecase.account.impl;

import com.bancopago.backend.application.secondaryports.repository.AccountRepository;
import com.bancopago.backend.application.usecase.account.CreateAccountUseCase;
import com.bancopago.backend.application.usecase.account.rulesvalidator.CreateAccountRulesValidator;
import com.bancopago.backend.domain.account.AccountDomain;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
public class CreateAccountUseCaseImpl implements CreateAccountUseCase {

    private final AccountRepository accountRepository;
    private final CreateAccountRulesValidator rulesValidator;

    public CreateAccountUseCaseImpl(AccountRepository accountRepository,
                                    CreateAccountRulesValidator rulesValidator) {
        this.accountRepository = accountRepository;
        this.rulesValidator = rulesValidator;
    }

    @Override
    @Transactional
    public Mono<AccountDomain> execute(AccountDomain account) {
        return rulesValidator.validate(account)
                .then(Mono.defer(() -> accountRepository.saveAccount(account)));
    }
}
