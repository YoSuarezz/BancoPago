package com.bancopago.backend.application.primaryports.interactor.person;

import com.bancopago.backend.application.primaryports.dto.person.request.CreatePersonRequest;
import com.bancopago.backend.application.primaryports.dto.person.response.CreateClientResponse;
import com.bancopago.backend.application.primaryports.dto.person.response.CreateEmployeeResponse;
import com.bancopago.backend.application.primaryports.mapper.person.PersonDTOMapper;
import com.bancopago.backend.application.primaryports.interactor.person.impl.CreatePersonInteractorImpl;
import com.bancopago.backend.application.usecase.person.CreatePersonUseCase;
import com.bancopago.backend.application.usecase.person.GenerateClientNumberUseCase;
import com.bancopago.backend.domain.enums.DocumentType;
import com.bancopago.backend.domain.enums.PersonType;
import com.bancopago.backend.domain.person.ClientDomain;
import com.bancopago.backend.domain.person.EmployeeDomain;
import com.bancopago.backend.domain.person.PersonDomain;
import com.bancopago.backend.domain.person.exceptions.InvalidPersonException;
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

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePersonInteractorTest {

    @Mock
    private CreatePersonUseCase createPersonUseCase;
    @Mock
    private GenerateClientNumberUseCase generateClientNumberUseCase;
    @Mock
    private PersonDTOMapper personDTOMapper;

    private CreatePersonInteractorImpl interactor;

    @BeforeEach
    void setUp() {
        interactor = new CreatePersonInteractorImpl(
                createPersonUseCase, generateClientNumberUseCase, personDTOMapper);
    }

    @Test
    @DisplayName("should generate client number and build ClientDomain for CLIENT type")
    void shouldCreateClientWithGeneratedNumber() {
        var request = clientRequest();
        var clientDomain = clientDomain("CLI-ABCD1234");
        var response = new CreateClientResponse(
                UUID.randomUUID(), "Ana", "100200300", "CC",
                "ana@example.com", null, "CLIENT", "CLI-ABCD1234", LocalDate.now());

        when(generateClientNumberUseCase.execute(PersonType.CLIENT))
                .thenReturn(Mono.just("CLI-ABCD1234"));
        when(personDTOMapper.toClientDomain(eq(request), anyString()))
                .thenReturn(clientDomain);
        when(createPersonUseCase.execute(clientDomain)).thenReturn(Mono.just(clientDomain));
        when(personDTOMapper.toCreatePersonResponse(clientDomain)).thenReturn(response);

        StepVerifier.create(interactor.execute(request))
                .assertNext(r -> {
                    assertInstanceOf(CreateClientResponse.class, r);
                    org.junit.jupiter.api.Assertions.assertEquals("CLI-ABCD1234",
                            ((CreateClientResponse) r).clientNumber());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should build EmployeeDomain without generating client number")
    void shouldCreateEmployeeWithoutClientNumber() {
        var request = employeeRequest();
        var employeeDomain = employeeDomain();
        var response = new CreateEmployeeResponse(
                UUID.randomUUID(), "Carlos", "200300400", "CC",
                "carlos@example.com", null, "EMPLOYEE", "Dev", "Eng", "CC-1", "PERMANENT");

        when(personDTOMapper.toEmployeeDomain(request)).thenReturn(employeeDomain);
        when(createPersonUseCase.execute(employeeDomain)).thenReturn(Mono.just(employeeDomain));
        when(personDTOMapper.toCreatePersonResponse(employeeDomain)).thenReturn(response);

        StepVerifier.create(interactor.execute(request))
                .assertNext(r -> {
                    assertInstanceOf(CreateEmployeeResponse.class, r);
                    org.junit.jupiter.api.Assertions.assertEquals("Dev",
                            ((CreateEmployeeResponse) r).position());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should fail when personType is null")
    void shouldFailWhenPersonTypeIsNull() {
        var request = new CreatePersonRequest(
                "Ana", "100200300", DocumentType.CC,
                "ana@example.com", null, null, null, null, null, null);

        StepVerifier.create(interactor.execute(request))
                .expectError(InvalidPersonException.class)
                .verify();
    }

    private static CreatePersonRequest clientRequest() {
        return new CreatePersonRequest(
                "Ana", "100200300", DocumentType.CC,
                "ana@example.com", "3001112233", PersonType.CLIENT,
                null, null, null, null);
    }

    private static CreatePersonRequest employeeRequest() {
        return new CreatePersonRequest(
                "Carlos", "200300400", DocumentType.CC,
                "carlos@example.com", "3009998877", PersonType.EMPLOYEE,
                "Dev", "Eng", "CC-1", "PERMANENT");
    }

    private static ClientDomain clientDomain(String clientNumber) {
        return new ClientDomain(
                "Ana",
                new DocumentNumber(DocumentType.CC, "100200300"),
                new Email("ana@example.com"),
                "3001112233",
                clientNumber
        );
    }

    private static EmployeeDomain employeeDomain() {
        return new EmployeeDomain(
                "Carlos",
                new DocumentNumber(DocumentType.CC, "200300400"),
                new Email("carlos@example.com"),
                "3009998877",
                "Dev", "Eng", "CC-1", "PERMANENT"
        );
    }
}
