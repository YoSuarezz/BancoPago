package com.bancopago.backend.infrastructure.primaryadapters.adapter.response;

import com.bancopago.backend.application.primaryports.dto.person.response.CreatePersonResponse;

import java.util.ArrayList;

public class PersonResponse extends Response<CreatePersonResponse> {

    public PersonResponse() {
        setMessages(new ArrayList<>());
        setData(new ArrayList<>());
    }
}
