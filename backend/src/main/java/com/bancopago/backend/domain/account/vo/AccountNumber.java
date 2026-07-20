package com.bancopago.backend.domain.account.vo;

import com.bancopago.backend.crosscutting.helpers.TextHelper;
import com.bancopago.backend.domain.account.AccountError;
import com.bancopago.backend.domain.account.exceptions.InvalidAccountException;

public record AccountNumber(String value) {

    public AccountNumber {
        value = TextHelper.applyTrim(value);
        if (TextHelper.isBlank(value)) {
            throw InvalidAccountException.create(AccountError.NUMBER_EMPTY);
        }
    }
}
