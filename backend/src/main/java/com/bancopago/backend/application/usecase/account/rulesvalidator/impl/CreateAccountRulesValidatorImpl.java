package com.bancopago.backend.application.usecase.account.rulesvalidator.impl;

import com.bancopago.backend.application.usecase.account.rulesvalidator.CreateAccountRulesValidator;
import com.bancopago.backend.application.usecase.account.rulesvalidator.rules.MaxAccountsPerOwnerRule;
import com.bancopago.backend.application.usecase.account.rulesvalidator.rules.OwnerExistsRule;
import com.bancopago.backend.application.usecase.account.rulesvalidator.rules.UniqueAccountNumberRule;
import com.bancopago.backend.application.usecase.account.rulesvalidator.rules.UniqueAccountTypePerOwnerRule;
import com.bancopago.backend.domain.account.AccountDomain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CreateAccountRulesValidatorImpl implements CreateAccountRulesValidator {

    private final OwnerExistsRule ownerExistsRule;
    private final MaxAccountsPerOwnerRule maxAccountsPerOwnerRule;
    private final UniqueAccountTypePerOwnerRule uniqueAccountTypePerOwnerRule;
    private final UniqueAccountNumberRule uniqueAccountNumberRule;

    public CreateAccountRulesValidatorImpl(OwnerExistsRule ownerExistsRule,
                                           MaxAccountsPerOwnerRule maxAccountsPerOwnerRule,
                                           UniqueAccountTypePerOwnerRule uniqueAccountTypePerOwnerRule,
                                           UniqueAccountNumberRule uniqueAccountNumberRule) {
        this.ownerExistsRule = ownerExistsRule;
        this.maxAccountsPerOwnerRule = maxAccountsPerOwnerRule;
        this.uniqueAccountTypePerOwnerRule = uniqueAccountTypePerOwnerRule;
        this.uniqueAccountNumberRule = uniqueAccountNumberRule;
    }

    @Override
    public Mono<Void> validate(AccountDomain account) {
        return ownerExistsRule.validate(account.getOwnerId())
                .then(maxAccountsPerOwnerRule.validate(account.getOwnerId()))
                .then(uniqueAccountTypePerOwnerRule.validate(account))
                .then(uniqueAccountNumberRule.validate(account.getNumber()));
    }
}
