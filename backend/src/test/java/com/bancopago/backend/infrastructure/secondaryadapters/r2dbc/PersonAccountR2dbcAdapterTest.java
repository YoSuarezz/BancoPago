package com.bancopago.backend.infrastructure.secondaryadapters.r2dbc;

import com.bancopago.backend.TestcontainersConfiguration;
import com.bancopago.backend.application.model.PersonQuery;
import com.bancopago.backend.application.secondaryports.repository.AccountRepository;
import com.bancopago.backend.application.secondaryports.repository.PersonRepository;
import com.bancopago.backend.domain.account.AccountDomain;
import com.bancopago.backend.domain.account.vo.AccountNumber;
import com.bancopago.backend.domain.enums.AccountStatus;
import com.bancopago.backend.domain.enums.AccountType;
import com.bancopago.backend.domain.enums.DocumentType;
import com.bancopago.backend.domain.enums.PersonType;
import com.bancopago.backend.domain.person.ClientDomain;
import com.bancopago.backend.domain.person.EmployeeDomain;
import com.bancopago.backend.domain.person.vo.DocumentNumber;
import com.bancopago.backend.domain.person.vo.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class PersonAccountR2dbcAdapterTest {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("should persist and find a client by id and document")
    void shouldPersistAndFindClient() {
        var client = new ClientDomain(
                "Ana Gomez",
                new DocumentNumber(DocumentType.CC, "100200300"),
                new Email("ana.gomez@example.com"),
                "3001112233",
                "CLI-100"
        );

        StepVerifier.create(
                        personRepository.savePerson(client)
                                .flatMap(saved -> personRepository.findPersonById(saved.getId()))
                )
                .assertNext(found -> {
                    assertEquals(client.getId(), found.getId());
                    assertEquals("Ana Gomez", found.getName());
                    assertEquals("100200300", found.getDocument());
                    assertEquals(DocumentType.CC, found.getDocumentType());
                    assertEquals("ana.gomez@example.com", found.getEmail());
                    assertEquals(PersonType.CLIENT, found.getPersonType());
                    assertEquals("CLI-100", ((ClientDomain) found).getClientNumber());
                    assertNotNull(((ClientDomain) found).getMembershipDate());
                })
                .verifyComplete();

        StepVerifier.create(personRepository.findPersonByDocument("100200300", "CC"))
                .assertNext(found -> assertEquals("ana.gomez@example.com", found.getEmail()))
                .verifyComplete();

        StepVerifier.create(personRepository.existsPersonByDocument("100200300"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("should persist an employee and update name")
    void shouldPersistAndUpdateEmployee() {
        var employee = new EmployeeDomain(
                "Carlos Ruiz",
                new DocumentNumber(DocumentType.CC, "900800700"),
                new Email("carlos.ruiz@example.com"),
                "3014445566",
                "Analyst",
                "Payments",
                "CC-01",
                "INDEFINITE"
        );

        StepVerifier.create(
                        personRepository.savePerson(employee)
                                .flatMap(saved -> {
                                    var updated = new EmployeeDomain(
                                            saved.getId(),
                                            saved.getDocumentNumber(),
                                            "Carlos Ruiz Updated",
                                            saved.getEmailObject(),
                                            saved.getPhone(),
                                            "Analyst",
                                            "Payments",
                                            "CC-01",
                                            "INDEFINITE"
                                    );
                                    return personRepository.savePerson(updated);
                                })
                                .flatMap(saved -> personRepository.findPersonById(saved.getId()))
                )
                .assertNext(found -> {
                    assertEquals("Carlos Ruiz Updated", found.getName());
                    assertEquals(PersonType.EMPLOYEE, found.getPersonType());
                    assertEquals("Analyst", ((EmployeeDomain) found).getPosition());
                    assertEquals("Payments", ((EmployeeDomain) found).getArea());
                    assertEquals("CC-01", ((EmployeeDomain) found).getCostCenter());
                    assertEquals("INDEFINITE", ((EmployeeDomain) found).getContractType());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should open account for person, find by number/owner, and update balance/status")
    void shouldPersistAccountAndUpdate() {
        var client = new ClientDomain(
                "Lucia Perez",
                new DocumentNumber(DocumentType.CC, "555666777"),
                new Email("lucia.perez@example.com"),
                "3027778899",
                "CLI-200"
        );

        StepVerifier.create(
                        personRepository.savePerson(client)
                                .flatMap(savedPerson -> {
                                    var account = new AccountDomain(
                                            savedPerson.getId(),
                                            new AccountNumber("2000000001"),
                                            AccountType.SAVINGS
                                    );
                                    return accountRepository.saveAccount(account)
                                            .flatMap(savedAccount -> {
                                                savedAccount.deposit(new BigDecimal("150000.00"));
                                                savedAccount.block();
                                                return accountRepository.saveAccount(savedAccount);
                                            });
                                })
                                .flatMap(saved -> accountRepository.findAccountByNumber("2000000001"))
                )
                .assertNext(found -> {
                    assertEquals("2000000001", found.getNumber());
                    assertEquals(AccountType.SAVINGS, found.getType());
                    assertEquals(AccountStatus.BLOCKED, found.getStatus());
                    assertEquals(0, found.getBalance().compareTo(new BigDecimal("150000.00")));
                })
                .verifyComplete();

        StepVerifier.create(
                        personRepository.findPersonByDocument("555666777", "CC")
                                .flatMapMany(person -> accountRepository.findAccountsByOwnerId(person.getId()))
                )
                .assertNext(account -> assertEquals("2000000001", account.getNumber()))
                .verifyComplete();

        StepVerifier.create(accountRepository.existsAccountByNumber("2000000001"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("should find persons by name ignoring case when paginating")
    void shouldFindPersonsPageByNameIgnoreCase() {
        var client = new ClientDomain(
                "Ana Gomez",
                new DocumentNumber(DocumentType.CC, "111222333"),
                new Email("ana.case@example.com"),
                "3009998877",
                "CLI-CASE"
        );

        StepVerifier.create(
                        personRepository.savePerson(client)
                                .then(personRepository.findPersonsPage(
                                        new PersonQuery(0, 10, "name", "ASC", "ana gomez", null)))
                )
                .assertNext(page -> {
                    assertEquals(1, page.content().size());
                    assertEquals("Ana Gomez", page.content().getFirst().getName());
                })
                .verifyComplete();

        StepVerifier.create(
                        personRepository.findPersonsPage(
                                new PersonQuery(0, 10, "name", "ASC", "ANA", null))
                )
                .assertNext(page -> {
                    assertEquals(1, page.content().size());
                    assertEquals("Ana Gomez", page.content().getFirst().getName());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should return empty when person or account does not exist")
    void shouldReturnEmptyWhenNotFound() {
        StepVerifier.create(personRepository.findPersonById(UUID.randomUUID()))
                .verifyComplete();

        StepVerifier.create(accountRepository.findAccountByNumber("9999999999"))
                .verifyComplete();

        StepVerifier.create(personRepository.existsPersonByDocument("does-not-exist"))
                .expectNext(false)
                .verifyComplete();
    }
}
