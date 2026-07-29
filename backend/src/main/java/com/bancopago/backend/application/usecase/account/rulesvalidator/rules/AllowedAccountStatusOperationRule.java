package com.bancopago.backend.application.usecase.account.rulesvalidator.rules;

import com.bancopago.backend.application.usecase.Rule;
import com.bancopago.backend.domain.enums.AccountOperation;

public interface AllowedAccountStatusOperationRule extends Rule<AccountOperation> {
}
