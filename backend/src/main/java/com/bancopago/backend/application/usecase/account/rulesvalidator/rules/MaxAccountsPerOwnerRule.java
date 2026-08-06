package com.bancopago.backend.application.usecase.account.rulesvalidator.rules;

import com.bancopago.backend.application.usecase.Rule;

import java.util.UUID;

public interface MaxAccountsPerOwnerRule extends Rule<UUID> {
}
