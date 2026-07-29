package com.bancopago.backend.application.usecase.account.impl;

import com.bancopago.backend.application.secondaryports.repository.AccountRepository;
import com.bancopago.backend.application.usecase.account.ChangeAccountStatusCommand;
import com.bancopago.backend.application.usecase.account.ChangeAccountStatusUseCase;
import com.bancopago.backend.application.usecase.account.rulesvalidator.ChangeAccountStatusRulesValidator;
import com.bancopago.backend.domain.account.AccountDomain;
import com.bancopago.backend.domain.account.exceptions.AccountNotFoundException;
import com.bancopago.backend.domain.enums.AccountOperation;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Orquesta: RulesValidator (política del UC) → cargar Domain → métodos de dominio → save.
 * Not-found vive aquí (como GetAccountBalance), no en una Rule que descarta el agregado.
 */
@Service
public class ChangeAccountStatusUseCaseImpl implements ChangeAccountStatusUseCase {

    private final AccountRepository accountRepository;
    private final ChangeAccountStatusRulesValidator rulesValidator;

    public ChangeAccountStatusUseCaseImpl(AccountRepository accountRepository,
                                          ChangeAccountStatusRulesValidator rulesValidator) {
        this.accountRepository = accountRepository;
        this.rulesValidator = rulesValidator;
    }

    @Override
    public Mono<AccountDomain> changeAccountStatus(ChangeAccountStatusCommand command) {
        return rulesValidator.validate(command)
                .then(Mono.defer(() -> accountRepository.findAccountById(command.accountId())))
                .switchIfEmpty(Mono.error(AccountNotFoundException.create(command.accountId())))
                .map(account -> applyDomainOperation(account, command.operation()))
                .flatMap(accountRepository::saveAccount);
    }

    /**
     * Solo despacha a Domain. La política BLOCK/UNBLOCK/CLOSE vs OPERATE
     * ya fue validada por {@code AllowedAccountStatusOperationRule}.
     */
    private AccountDomain applyDomainOperation(AccountDomain account, AccountOperation operation) {
        switch (operation) {
            case BLOCK -> account.block();
            case UNBLOCK -> account.unblock();
            case CLOSE -> account.close();
            case OPERATE -> throw new IllegalStateException(
                    "OPERATE must be rejected by AllowedAccountStatusOperationRule before apply");
        }
        return account;
    }
}
