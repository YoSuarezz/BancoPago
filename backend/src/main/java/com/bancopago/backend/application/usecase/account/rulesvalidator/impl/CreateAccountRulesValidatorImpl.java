package com.bancopago.backend.application.usecase.account.rulesvalidator.impl;

import com.bancopago.backend.application.usecase.account.rulesvalidator.CreateAccountRulesValidator;
import com.bancopago.backend.application.usecase.account.rulesvalidator.rules.MaxAccountsPerOwnerRule;
import com.bancopago.backend.application.usecase.account.rulesvalidator.rules.OwnerExistsRule;
import com.bancopago.backend.application.usecase.account.rulesvalidator.rules.UniqueAccountNumberRule;
import com.bancopago.backend.domain.account.AccountDomain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Orquesta las {@link com.bancopago.backend.application.usecase.Rule} con estado
 * del caso CreateAccount. No contiene lógica de repositorio: solo las ejecuta.
 */
@Component
public class CreateAccountRulesValidatorImpl implements CreateAccountRulesValidator {

    private final OwnerExistsRule ownerExistsRule;
    private final MaxAccountsPerOwnerRule maxAccountsPerOwnerRule;
    private final UniqueAccountNumberRule uniqueAccountNumberRule;

    public CreateAccountRulesValidatorImpl(OwnerExistsRule ownerExistsRule,
                                           MaxAccountsPerOwnerRule maxAccountsPerOwnerRule,
                                           UniqueAccountNumberRule uniqueAccountNumberRule) {
        this.ownerExistsRule = ownerExistsRule;
        this.maxAccountsPerOwnerRule = maxAccountsPerOwnerRule;
        this.uniqueAccountNumberRule = uniqueAccountNumberRule;
    }

    @Override
    public Mono<Void> validate(AccountDomain account) {
        return ownerExistsRule.validate(account.getOwnerId())
                .then(maxAccountsPerOwnerRule.validate(account.getOwnerId()))
                .then(uniqueAccountNumberRule.validate(account.getNumber()));
    }
}
