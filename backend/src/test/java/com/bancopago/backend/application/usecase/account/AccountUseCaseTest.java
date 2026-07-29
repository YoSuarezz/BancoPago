package com.bancopago.backend.application.usecase.account;

import com.bancopago.backend.application.secondaryports.repository.AccountRepository;
import com.bancopago.backend.application.usecase.account.impl.ChangeAccountStatusUseCaseImpl;
import com.bancopago.backend.application.usecase.account.impl.CreateAccountUseCaseImpl;
import com.bancopago.backend.application.usecase.account.impl.GetAccountBalanceUseCaseImpl;
import com.bancopago.backend.application.usecase.account.rulesvalidator.ChangeAccountStatusRulesValidator;
import com.bancopago.backend.application.usecase.account.rulesvalidator.CreateAccountRulesValidator;
import com.bancopago.backend.domain.account.AccountDomain;
import com.bancopago.backend.domain.account.exceptions.AccountNotFoundException;
import com.bancopago.backend.domain.account.vo.AccountNumber;
import com.bancopago.backend.domain.enums.AccountOperation;
import com.bancopago.backend.domain.enums.AccountStatus;
import com.bancopago.backend.domain.enums.AccountType;
import com.bancopago.backend.domain.person.exceptions.PersonNotFoundException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountUseCaseTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountNumberGenerator accountNumberGenerator;
    @Mock
    private CreateAccountRulesValidator createAccountRulesValidator;
    @Mock
    private ChangeAccountStatusRulesValidator changeAccountStatusRulesValidator;

    private CreateAccountUseCaseImpl createAccountUseCase;
    private ChangeAccountStatusUseCaseImpl changeAccountStatusUseCase;
    private GetAccountBalanceUseCaseImpl getAccountBalanceUseCase;

    @BeforeEach
    void setUp() {
        createAccountUseCase = new CreateAccountUseCaseImpl(
                accountRepository, accountNumberGenerator, createAccountRulesValidator);
        changeAccountStatusUseCase = new ChangeAccountStatusUseCaseImpl(
                accountRepository, changeAccountStatusRulesValidator);
        getAccountBalanceUseCase = new GetAccountBalanceUseCaseImpl(accountRepository);
    }

    @Test
    @DisplayName("should create account when owner exists")
    void shouldCreateAccountWhenOwnerExists() {
        UUID ownerId = UUID.randomUUID();
        var number = new AccountNumber("1234567890");

        when(accountNumberGenerator.generateUniqueAccountNumber()).thenReturn(Mono.just(number));
        when(createAccountRulesValidator.validate(any(AccountDomain.class))).thenReturn(Mono.empty());
        when(accountRepository.saveAccount(any(AccountDomain.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(createAccountUseCase.createAccount(
                        new CreateAccountCommand(ownerId, AccountType.SAVINGS)))
                .assertNext(result -> {
                    assertEquals(ownerId, result.getOwnerId());
                    assertEquals("1234567890", result.getNumber());
                    assertEquals(AccountType.SAVINGS, result.getType());
                    assertEquals(AccountStatus.ACTIVE, result.getStatus());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should fail when owner does not exist")
    void shouldFailWhenOwnerDoesNotExist() {
        UUID ownerId = UUID.randomUUID();
        var number = new AccountNumber("1234567890");

        when(accountNumberGenerator.generateUniqueAccountNumber()).thenReturn(Mono.just(number));
        when(createAccountRulesValidator.validate(any(AccountDomain.class)))
                .thenReturn(Mono.error(PersonNotFoundException.create(ownerId)));

        StepVerifier.create(createAccountUseCase.createAccount(
                        new CreateAccountCommand(ownerId, AccountType.CHECKING)))
                .expectError(PersonNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("should block active account")
    void shouldBlockActiveAccount() {
        UUID ownerId = UUID.randomUUID();
        var account = new AccountDomain(ownerId, new AccountNumber("1234567890"), AccountType.SAVINGS);
        var command = new ChangeAccountStatusCommand(account.getId(), AccountOperation.BLOCK);

        when(changeAccountStatusRulesValidator.validate(any(ChangeAccountStatusCommand.class)))
                .thenReturn(Mono.empty());
        when(accountRepository.findAccountById(account.getId())).thenReturn(Mono.just(account));
        when(accountRepository.saveAccount(any(AccountDomain.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(changeAccountStatusUseCase.changeAccountStatus(command))
                .assertNext(result -> assertEquals(AccountStatus.BLOCKED, result.getStatus()))
                .verifyComplete();
    }

    @Test
    @DisplayName("should fail balance query when account missing")
    void shouldFailBalanceWhenAccountMissing() {
        UUID missingId = UUID.randomUUID();
        when(accountRepository.findAccountById(missingId)).thenReturn(Mono.empty());

        StepVerifier.create(getAccountBalanceUseCase.getAccountBalance(missingId))
                .expectError(AccountNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("should return account balance")
    void shouldReturnAccountBalance() {
        UUID ownerId = UUID.randomUUID();
        var account = new AccountDomain(ownerId, new AccountNumber("1234567890"), AccountType.SAVINGS);
        when(accountRepository.findAccountById(account.getId())).thenReturn(Mono.just(account));

        StepVerifier.create(getAccountBalanceUseCase.getAccountBalance(account.getId()))
                .assertNext(result -> {
                    assertEquals(account.getId(), result.getId());
                    assertEquals(account.getBalance(), result.getBalance());
                    assertEquals(AccountStatus.ACTIVE, result.getStatus());
                })
                .verifyComplete();
    }
}
