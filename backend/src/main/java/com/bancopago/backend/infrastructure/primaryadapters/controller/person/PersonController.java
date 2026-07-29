package com.bancopago.backend.infrastructure.primaryadapters.controller.person;

import com.bancopago.backend.application.primaryports.dto.person.request.CreatePersonRequest;
import com.bancopago.backend.application.primaryports.dto.person.response.CreatePersonResponse;
import com.bancopago.backend.application.primaryports.dto.person.response.GetPersonByIdResponse;
import com.bancopago.backend.application.primaryports.interactor.person.CreatePersonInteractor;
import com.bancopago.backend.application.primaryports.interactor.person.GetPersonByIdInteractor;
import com.bancopago.backend.infrastructure.ResponseMessages;
import com.bancopago.backend.infrastructure.primaryadapters.adapter.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/persons")
@Tag(name = "Persons")
public class PersonController {

    private final CreatePersonInteractor createPersonInteractor;
    private final GetPersonByIdInteractor getPersonByIdInteractor;

    public PersonController(
            CreatePersonInteractor createPersonInteractor,
            GetPersonByIdInteractor getPersonByIdInteractor) {
        this.createPersonInteractor = createPersonInteractor;
        this.getPersonByIdInteractor = getPersonByIdInteractor;
    }

    @PostMapping
    @Operation(summary = "Create a person")
    public Mono<ResponseEntity<ApiResponse<CreatePersonResponse>>> createPerson(
            @RequestBody @Valid CreatePersonRequest request) {
        return createPersonInteractor.execute(request)
                .map(dto -> ApiResponse.of(dto, ResponseMessages.PERSON_CREATED))
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a person by id")
    public Mono<ResponseEntity<ApiResponse<GetPersonByIdResponse>>> getPersonById(@PathVariable UUID id) {
        return getPersonByIdInteractor.execute(id)
                .map(ApiResponse::of)
                .map(ResponseEntity::ok);
    }
}
