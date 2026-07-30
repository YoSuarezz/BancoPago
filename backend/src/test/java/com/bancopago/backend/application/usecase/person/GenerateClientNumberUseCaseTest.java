package com.bancopago.backend.application.usecase.person;

import com.bancopago.backend.application.usecase.person.impl.GenerateClientNumberUseCaseImpl;
import com.bancopago.backend.application.usecase.person.rulesvalidator.rules.IsClientRule;
import com.bancopago.backend.domain.enums.PersonType;
import com.bancopago.backend.domain.person.exceptions.InvalidPersonException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateClientNumberUseCaseTest {

    @Mock
    private IsClientRule isClientRule;

    private GenerateClientNumberUseCaseImpl generateClientNumberUseCase;

    @BeforeEach
    void setUp() {
        generateClientNumberUseCase = new GenerateClientNumberUseCaseImpl(isClientRule);
    }

    @Test
    @DisplayName("should generate a client number prefixed with CLI- for CLIENT type")
    void shouldGenerateClientNumberForClientType() {
        when(isClientRule.validate(PersonType.CLIENT)).thenReturn(Mono.empty());

        StepVerifier.create(generateClientNumberUseCase.execute(PersonType.CLIENT))
                .assertNext(clientNumber -> {
                    assertNotNull(clientNumber);
                    assertTrue(clientNumber.startsWith("CLI-"),
                            "Client number must start with CLI-");
                    assertTrue(clientNumber.length() > 4,
                            "Client number must have a suffix after CLI-");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should fail when person type is not CLIENT")
    void shouldFailWhenNotClientType() {
        when(isClientRule.validate(PersonType.EMPLOYEE))
                .thenReturn(Mono.error(InvalidPersonException.create(
                        com.bancopago.backend.domain.person.PersonError.TYPE_REQUIRED)));

        StepVerifier.create(generateClientNumberUseCase.execute(PersonType.EMPLOYEE))
                .expectError(InvalidPersonException.class)
                .verify();
    }
}
