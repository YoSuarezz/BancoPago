package com.bancopago.backend.application.usecase.person;

import com.bancopago.backend.application.secondaryports.repository.PersonRepository;
import com.bancopago.backend.application.usecase.person.impl.CreatePersonUseCaseImpl;
import com.bancopago.backend.application.usecase.person.impl.GetPersonByIdUseCaseImpl;
import com.bancopago.backend.application.usecase.person.rulesvalidator.CreatePersonRulesValidator;
import com.bancopago.backend.domain.enums.DocumentType;
import com.bancopago.backend.domain.person.ClientDomain;
import com.bancopago.backend.domain.person.exceptions.DuplicateDocumentException;
import com.bancopago.backend.domain.person.exceptions.PersonNotFoundException;
import com.bancopago.backend.domain.person.vo.DocumentNumber;
import com.bancopago.backend.domain.person.vo.Email;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonUseCaseTest {

    @Mock
    private PersonRepository personRepository;
    @Mock
    private CreatePersonRulesValidator rulesValidator;

    private CreatePersonUseCaseImpl createPersonUseCase;
    private GetPersonByIdUseCaseImpl getPersonByIdUseCase;

    @BeforeEach
    void setUp() {
        createPersonUseCase = new CreatePersonUseCaseImpl(personRepository, rulesValidator);
        getPersonByIdUseCase = new GetPersonByIdUseCaseImpl(personRepository);
    }

    @Test
    @DisplayName("should create person when document is unique")
    void shouldCreatePersonWhenDocumentIsUnique() {
        var domain = new ClientDomain(
                "Ana Gomez",
                new DocumentNumber(DocumentType.CC, "100200300"),
                new Email("ana@example.com"),
                "3001112233",
                "CLI-1"
        );

        when(rulesValidator.validate(domain)).thenReturn(Mono.empty());
        when(personRepository.savePerson(domain)).thenReturn(Mono.just(domain));

        StepVerifier.create(createPersonUseCase.execute(domain))
                .assertNext(result -> {
                    assertEquals("Ana Gomez", result.getName());
                    assertEquals("100200300", result.getDocument());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should fail when document already exists")
    void shouldFailWhenDocumentAlreadyExists() {
        var domain = new ClientDomain(
                "Ana Gomez",
                new DocumentNumber(DocumentType.CC, "100200300"),
                new Email("ana@example.com"),
                "3001112233",
                "CLI-1"
        );

        when(rulesValidator.validate(domain))
                .thenReturn(Mono.error(DuplicateDocumentException.create("100200300")));

        StepVerifier.create(createPersonUseCase.execute(domain))
                .expectError(DuplicateDocumentException.class)
                .verify();
    }

    @Test
    @DisplayName("should fail when person id is not found")
    void shouldFailWhenPersonIdIsNotFound() {
        UUID missingId = UUID.randomUUID();
        when(personRepository.findPersonById(missingId)).thenReturn(Mono.empty());

        StepVerifier.create(getPersonByIdUseCase.execute(missingId))
                .expectError(PersonNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("should get person by id")
    void shouldGetPersonById() {
        var domain = new ClientDomain(
                "Ana Gomez",
                new DocumentNumber(DocumentType.CC, "100200300"),
                new Email("ana@example.com"),
                "3001112233",
                "CLI-1"
        );
        when(personRepository.findPersonById(domain.getId())).thenReturn(Mono.just(domain));

        StepVerifier.create(getPersonByIdUseCase.execute(domain.getId()))
                .assertNext(result -> assertEquals(domain.getId(), result.getId()))
                .verifyComplete();
    }
}
