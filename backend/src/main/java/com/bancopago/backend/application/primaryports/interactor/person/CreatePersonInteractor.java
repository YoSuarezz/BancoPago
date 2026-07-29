package com.bancopago.backend.application.primaryports.interactor.person;

import com.bancopago.backend.application.primaryports.dto.person.request.CreatePersonRequest;
import com.bancopago.backend.application.primaryports.dto.person.response.CreatePersonResponse;
import com.bancopago.backend.application.primaryports.interactor.InteractorWithReturn;

public interface CreatePersonInteractor
        extends InteractorWithReturn<CreatePersonRequest, CreatePersonResponse> {
}
