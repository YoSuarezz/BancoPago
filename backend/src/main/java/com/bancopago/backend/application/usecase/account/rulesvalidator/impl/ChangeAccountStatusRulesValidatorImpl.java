package com.bancopago.backend.application.usecase.account.rulesvalidator.impl;

import com.bancopago.backend.application.usecase.account.ChangeAccountStatusCommand;
import com.bancopago.backend.application.usecase.account.rulesvalidator.ChangeAccountStatusRulesValidator;
import com.bancopago.backend.application.usecase.account.rulesvalidator.rules.AllowedAccountStatusOperationRule;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Solo orquesta Rules de política del UC.
 * Existencia de cuenta: UseCase (find + NotFound), igual que GetAccountBalance.
 */
@Component
public class ChangeAccountStatusRulesValidatorImpl implements ChangeAccountStatusRulesValidator {

    private final AllowedAccountStatusOperationRule allowedAccountStatusOperationRule;

    public ChangeAccountStatusRulesValidatorImpl(
            AllowedAccountStatusOperationRule allowedAccountStatusOperationRule) {
        this.allowedAccountStatusOperationRule = allowedAccountStatusOperationRule;
    }

    @Override
    public Mono<Void> validate(ChangeAccountStatusCommand command) {
        return allowedAccountStatusOperationRule.validate(command.operation());
    }
}
