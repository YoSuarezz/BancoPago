package com.bancopago.backend.application.primaryports.interactor.person;

import com.bancopago.backend.application.primaryports.dto.person.response.GetPersonByIdResponse;
import com.bancopago.backend.application.primaryports.interactor.InteractorWithReturn;

import java.util.UUID;

public interface GetPersonByIdInteractor
        extends InteractorWithReturn<UUID, GetPersonByIdResponse> {
}
