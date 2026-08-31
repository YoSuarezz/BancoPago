package com.bancopago.backend.application.usecase.transfer.rulesvalidator.impl;

import com.bancopago.backend.application.usecase.transfer.rulesvalidator.TransferRulesValidator;
import com.bancopago.backend.application.usecase.transfer.rulesvalidator.rules.SourceAccountExistsRule;
import com.bancopago.backend.application.usecase.transfer.rulesvalidator.rules.SourceAccountOperableRule;
import com.bancopago.backend.application.usecase.transfer.rulesvalidator.rules.SufficientBalanceRule;
import com.bancopago.backend.application.usecase.transfer.rulesvalidator.rules.TargetAccountExistsRule;
import com.bancopago.backend.domain.transfer.TransferDomain;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TransferRulesValidatorImpl implements TransferRulesValidator {

    private final SourceAccountExistsRule sourceAccountExistsRule;
    private final TargetAccountExistsRule targetAccountExistsRule;
    private final SourceAccountOperableRule sourceAccountOperableRule;
    private final SufficientBalanceRule sufficientBalanceRule;

    public TransferRulesValidatorImpl(SourceAccountExistsRule sourceAccountExistsRule,
                                       TargetAccountExistsRule targetAccountExistsRule,
                                       SourceAccountOperableRule sourceAccountOperableRule,
                                       SufficientBalanceRule sufficientBalanceRule) {
        this.sourceAccountExistsRule = sourceAccountExistsRule;
        this.targetAccountExistsRule = targetAccountExistsRule;
        this.sourceAccountOperableRule = sourceAccountOperableRule;
        this.sufficientBalanceRule = sufficientBalanceRule;
    }

    @Override
    public Mono<Void> validate(TransferDomain transfer) {
        return sourceAccountExistsRule.validate(transfer.getSourceAccountNumber())
                .then(targetAccountExistsRule.validate(transfer.getTargetAccountNumber()))
                .then(sourceAccountOperableRule.validate(transfer.getSourceAccountNumber()))
                .then(sufficientBalanceRule.validate(transfer));
    }
}
