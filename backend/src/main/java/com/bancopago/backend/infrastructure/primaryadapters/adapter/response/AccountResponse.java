package com.bancopago.backend.infrastructure.primaryadapters.adapter.response;

import com.bancopago.backend.application.primaryports.dto.account.response.CreateAccountResponse;

import java.util.ArrayList;

public class AccountResponse extends Response<CreateAccountResponse> {

    public AccountResponse() {
        setMessages(new ArrayList<>());
        setData(new ArrayList<>());
    }
}
