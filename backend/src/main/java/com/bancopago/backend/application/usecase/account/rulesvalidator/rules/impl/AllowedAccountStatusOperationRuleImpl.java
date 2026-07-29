package com.bancopago.backend.application.usecase.account.rulesvalidator.rules.impl;

import com.bancopago.backend.application.usecase.account.rulesvalidator.rules.AllowedAccountStatusOperationRule;
import com.bancopago.backend.domain.account.exceptions.UnsupportedAccountStatusOperationException;
import com.bancopago.backend.domain.enums.AccountOperation;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Política de aplicación: ChangeAccountStatus solo admite BLOCK / UNBLOCK / CLOSE.
 */
@Component
public class AllowedAccountStatusOperationRuleImpl implements AllowedAccountStatusOperationRule {

    @Override
    public Mono<Void> validate(AccountOperation operation) {
        if (operation == AccountOperation.BLOCK
                || operation == AccountOperation.UNBLOCK
                || operation == AccountOperation.CLOSE) {
            return Mono.empty();
        }
        return Mono.error(UnsupportedAccountStatusOperationException.create(operation));
    }
}
