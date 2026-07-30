package com.bancopago.backend.infrastructure.primaryadapters.controller.person;

import com.bancopago.backend.application.primaryports.dto.person.request.CreatePersonRequest;
import com.bancopago.backend.application.primaryports.dto.person.request.PersonPageRequest;
import com.bancopago.backend.application.primaryports.dto.person.response.CreatePersonResponse;
import com.bancopago.backend.application.primaryports.dto.person.response.GetPersonByIdResponse;
import com.bancopago.backend.application.primaryports.dto.person.response.ListPersonResponse;
import com.bancopago.backend.application.primaryports.interactor.person.CreatePersonInteractor;
import com.bancopago.backend.application.primaryports.interactor.person.GetPersonByIdInteractor;
import com.bancopago.backend.application.primaryports.interactor.person.ListPersonsInteractor;
import com.bancopago.backend.application.primaryports.interactor.person.ListPersonsPagedInteractor;
import com.bancopago.backend.infrastructure.ResponseMessages;
import com.bancopago.backend.infrastructure.primaryadapters.adapter.response.ApiResponse;
import com.bancopago.backend.infrastructure.primaryadapters.adapter.response.HttpResponses;
import com.bancopago.backend.infrastructure.primaryadapters.adapter.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final ListPersonsInteractor listPersonsInteractor;
    private final ListPersonsPagedInteractor listPersonsPagedInteractor;

    public PersonController(
            CreatePersonInteractor createPersonInteractor,
            GetPersonByIdInteractor getPersonByIdInteractor,
            ListPersonsInteractor listPersonsInteractor,
            ListPersonsPagedInteractor listPersonsPagedInteractor) {
        this.createPersonInteractor = createPersonInteractor;
        this.getPersonByIdInteractor = getPersonByIdInteractor;
        this.listPersonsInteractor = listPersonsInteractor;
        this.listPersonsPagedInteractor = listPersonsPagedInteractor;
    }

    @PostMapping
    @Operation(summary = "Create a person")
    public Mono<ResponseEntity<ApiResponse<CreatePersonResponse>>> createPerson(
            @RequestBody @Valid CreatePersonRequest request) {
        return HttpResponses.created(
                createPersonInteractor.execute(request),
                ResponseMessages.PERSON_CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a person by id")
    public Mono<ResponseEntity<ApiResponse<GetPersonByIdResponse>>> getPersonById(@PathVariable UUID id) {
        return HttpResponses.ok(getPersonByIdInteractor.execute(id));
    }

    @GetMapping
    @Operation(summary = "List persons")
    public Mono<ResponseEntity<ApiResponse<ListPersonResponse>>> listPersons() {
        return HttpResponses.okList(listPersonsInteractor.execute());
    }

    @GetMapping("/search")
    @Operation(summary = "Search persons with pagination")
    public Mono<ResponseEntity<PageResponse<ListPersonResponse>>> searchPersons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String personType) {
        var request = new PersonPageRequest(page, size, sortBy, sortDirection, name,
                PersonPageRequest.parsePersonType(personType));
        return HttpResponses.okPage(listPersonsPagedInteractor.execute(request));
    }
}
