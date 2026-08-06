package com.bancopago.backend.application.usecase.account;

import com.bancopago.backend.application.secondaryports.repository.AccountRepository;
import com.bancopago.backend.application.usecase.account.impl.BlockAccountUseCaseImpl;
import com.bancopago.backend.application.usecase.account.impl.CloseAccountUseCaseImpl;
import com.bancopago.backend.application.usecase.account.impl.CreateAccountUseCaseImpl;
import com.bancopago.backend.application.usecase.account.impl.GetAccountBalanceUseCaseImpl;
import com.bancopago.backend.application.usecase.account.impl.ListAccountsByOwnerUseCaseImpl;
import com.bancopago.backend.application.usecase.account.impl.StreamAccountBalanceUseCaseImpl;
import com.bancopago.backend.application.usecase.account.impl.UnblockAccountUseCaseImpl;
import com.bancopago.backend.application.usecase.account.rulesvalidator.CreateAccountRulesValidator;
import com.bancopago.backend.application.usecase.account.rulesvalidator.StreamAccountBalanceRulesValidator;
import com.bancopago.backend.domain.account.AccountDomain;
import com.bancopago.backend.domain.account.exceptions.AccountNotFoundException;
import com.bancopago.backend.domain.account.vo.AccountNumber;
import com.bancopago.backend.domain.enums.AccountStatus;
import com.bancopago.backend.domain.enums.AccountType;
import com.bancopago.backend.domain.person.exceptions.PersonNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountUseCaseTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CreateAccountRulesValidator createAccountRulesValidator;
    @Mock
    private StreamAccountBalanceRulesValidator streamAccountBalanceRulesValidator;
    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    private CreateAccountUseCaseImpl createAccountUseCase;
    private BlockAccountUseCaseImpl blockAccountUseCase;
    private UnblockAccountUseCaseImpl unblockAccountUseCase;
    private CloseAccountUseCaseImpl closeAccountUseCase;
    private GetAccountBalanceUseCaseImpl getAccountBalanceUseCase;
    private StreamAccountBalanceUseCaseImpl streamAccountBalanceUseCase;
    private ListAccountsByOwnerUseCaseImpl listAccountsByOwnerUseCase;

    @BeforeEach
    void setUp() {
        createAccountUseCase = new CreateAccountUseCaseImpl(
                accountRepository, createAccountRulesValidator, accountNumberGenerator);
        blockAccountUseCase = new BlockAccountUseCaseImpl(accountRepository);
        unblockAccountUseCase = new UnblockAccountUseCaseImpl(accountRepository);
        closeAccountUseCase = new CloseAccountUseCaseImpl(accountRepository);
        getAccountBalanceUseCase = new GetAccountBalanceUseCaseImpl(accountRepository);
        streamAccountBalanceUseCase = new StreamAccountBalanceUseCaseImpl(
                accountRepository, streamAccountBalanceRulesValidator);
        listAccountsByOwnerUseCase = new ListAccountsByOwnerUseCaseImpl(accountRepository);
    }

    @Test
    @DisplayName("should create account when owner exists")
    void shouldCreateAccountWhenOwnerExists() {
        UUID ownerId = UUID.randomUUID();
        var command = new CreateAccountCommand(ownerId, AccountType.SAVINGS);

        when(accountNumberGenerator.generateUniqueAccountNumber())
                .thenReturn(Mono.just(new AccountNumber("1234567890")));
        when(createAccountRulesValidator.validate(any(AccountDomain.class))).thenReturn(Mono.empty());
        when(accountRepository.saveAccount(any(AccountDomain.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(createAccountUseCase.execute(command))
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
        var command = new CreateAccountCommand(ownerId, AccountType.CHECKING);

        when(accountNumberGenerator.generateUniqueAccountNumber())
                .thenReturn(Mono.just(new AccountNumber("1234567890")));
        when(createAccountRulesValidator.validate(any(AccountDomain.class)))
                .thenReturn(Mono.error(PersonNotFoundException.create(ownerId)));

        StepVerifier.create(createAccountUseCase.execute(command))
                .expectError(PersonNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("should block active account")
    void shouldBlockActiveAccount() {
        UUID ownerId = UUID.randomUUID();
        var account = new AccountDomain(ownerId, new AccountNumber("1234567890"), AccountType.SAVINGS);

        when(accountRepository.findAccountById(account.getId())).thenReturn(Mono.just(account));
        when(accountRepository.saveAccount(any(AccountDomain.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(blockAccountUseCase.execute(account.getId()))
                .assertNext(result -> assertEquals(AccountStatus.BLOCKED, result.getStatus()))
                .verifyComplete();
    }

    @Test
    @DisplayName("should unblock blocked account")
    void shouldUnblockBlockedAccount() {
        UUID ownerId = UUID.randomUUID();
        var account = new AccountDomain(ownerId, new AccountNumber("1234567890"), AccountType.SAVINGS);
        account.block();

        when(accountRepository.findAccountById(account.getId())).thenReturn(Mono.just(account));
        when(accountRepository.saveAccount(any(AccountDomain.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(unblockAccountUseCase.execute(account.getId()))
                .assertNext(result -> assertEquals(AccountStatus.ACTIVE, result.getStatus()))
                .verifyComplete();
    }

    @Test
    @DisplayName("should close active account with zero balance")
    void shouldCloseActiveAccount() {
        UUID ownerId = UUID.randomUUID();
        var account = new AccountDomain(ownerId, new AccountNumber("1234567890"), AccountType.SAVINGS);

        when(accountRepository.findAccountById(account.getId())).thenReturn(Mono.just(account));
        when(accountRepository.saveAccount(any(AccountDomain.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(closeAccountUseCase.execute(account.getId()))
                .assertNext(result -> assertEquals(AccountStatus.INACTIVE, result.getStatus()))
                .verifyComplete();
    }

    @Test
    @DisplayName("should fail balance query when account missing")
    void shouldFailBalanceWhenAccountMissing() {
        UUID missingId = UUID.randomUUID();
        when(accountRepository.findAccountById(missingId)).thenReturn(Mono.empty());

        StepVerifier.create(getAccountBalanceUseCase.execute(missingId))
                .expectError(AccountNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("should return account balance")
    void shouldReturnAccountBalance() {
        UUID ownerId = UUID.randomUUID();
        var account = new AccountDomain(ownerId, new AccountNumber("1234567890"), AccountType.SAVINGS);
        when(accountRepository.findAccountById(account.getId())).thenReturn(Mono.just(account));

        StepVerifier.create(getAccountBalanceUseCase.execute(account.getId()))
                .assertNext(result -> {
                    assertEquals(account.getId(), result.getId());
                    assertEquals(account.getBalance(), result.getBalance());
                    assertEquals(AccountStatus.ACTIVE, result.getStatus());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("should stream account balance on interval")
    void shouldStreamAccountBalance() {
        UUID ownerId = UUID.randomUUID();
        var account = new AccountDomain(ownerId, new AccountNumber("1234567890"), AccountType.SAVINGS);
        when(streamAccountBalanceRulesValidator.validate(account.getId())).thenReturn(Mono.empty());
        when(accountRepository.findAccountById(account.getId())).thenReturn(Mono.just(account));

        StepVerifier.withVirtualTime(() -> streamAccountBalanceUseCase.execute(account.getId()).take(2))
                .expectSubscription()
                .assertNext(result -> assertEquals(account.getId(), result.getId()))
                .thenAwait(Duration.ofSeconds(5))
                .assertNext(result -> assertEquals(account.getId(), result.getId()))
                .verifyComplete();
    }

    @Test
    @DisplayName("should fail stream when account missing")
    void shouldFailStreamWhenAccountMissing() {
        UUID missingId = UUID.randomUUID();
        when(streamAccountBalanceRulesValidator.validate(missingId))
                .thenReturn(Mono.error(AccountNotFoundException.create(missingId)));

        StepVerifier.create(streamAccountBalanceUseCase.execute(missingId).take(1))
                .expectError(AccountNotFoundException.class)
                .verify();
    }

    @Test
    @DisplayName("should list accounts by owner")
    void shouldListAccountsByOwner() {
        UUID ownerId = UUID.randomUUID();
        var account = new AccountDomain(ownerId, new AccountNumber("1234567890"), AccountType.SAVINGS);
        when(accountRepository.findAccountsByOwnerId(ownerId)).thenReturn(Flux.just(account));

        StepVerifier.create(listAccountsByOwnerUseCase.execute(ownerId))
                .assertNext(list -> {
                    assertEquals(1, list.size());
                    assertEquals(account.getId(), list.getFirst().getId());
                })
                .verifyComplete();
    }
}
