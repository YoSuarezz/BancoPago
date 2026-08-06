package com.bancopago.backend.application.usecase.account.rulesvalidator.rules;

import com.bancopago.backend.application.usecase.Rule;
import com.bancopago.backend.domain.account.AccountDomain;

public interface UniqueAccountTypePerOwnerRule extends Rule<AccountDomain> {
}
