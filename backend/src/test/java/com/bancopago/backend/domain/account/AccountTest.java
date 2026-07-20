package com.bancopago.backend.domain.account;

import com.bancopago.backend.domain.account.exceptions.AccountBlockedException;
import com.bancopago.backend.domain.account.exceptions.InsufficientBalanceException;
import com.bancopago.backend.domain.account.exceptions.InvalidAccountException;
import com.bancopago.backend.domain.account.exceptions.InvalidAccountStateException;
import com.bancopago.backend.domain.account.exceptions.InvalidAmountException;
import com.bancopago.backend.domain.account.vo.AccountNumber;
import com.bancopago.backend.domain.enums.AccountStatus;
import com.bancopago.backend.domain.enums.AccountType;
import com.bancopago.backend.domain.enums.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    private static final UUID OWNER_ID = UUID.randomUUID();
    private AccountDomain account;

    @BeforeEach
    void setUp() {
        account = new AccountDomain(OWNER_ID, new AccountNumber("001-123456"), AccountType.SAVINGS);
    }

    @Nested
    @DisplayName("Account creation")
    class AccountCreation {

        @Test
        @DisplayName("should create account with ACTIVE status and zero balance")
        void shouldCreateAccountWithActiveStatusAndZeroBalance() {
            assertEquals(AccountStatus.ACTIVE, account.getStatus());
            assertEquals(BigDecimal.ZERO, account.getBalance());
            assertEquals(Currency.COP, account.getCurrency());
            assertEquals(AccountType.SAVINGS, account.getType());
            assertEquals("001-123456", account.getNumber());
            assertNotNull(account.getId());
        }

        @Test
        @DisplayName("should throw exception when number is null")
        void shouldThrowExceptionWhenNumberIsNull() {
            assertThrows(InvalidAccountException.class,
                    () -> new AccountNumber(null));
        }

        @Test
        @DisplayName("should throw exception when number is empty")
        void shouldThrowExceptionWhenNumberIsEmpty() {
            assertThrows(InvalidAccountException.class,
                    () -> new AccountNumber(""));
        }

        @Test
        @DisplayName("should throw exception when type is null")
        void shouldThrowExceptionWhenTypeIsNull() {
            assertThrows(InvalidAccountException.class,
                    () -> new AccountDomain(OWNER_ID, new AccountNumber("001-123456"), null));
        }

        @Test
        @DisplayName("should throw exception when initial balance is negative")
        void shouldThrowExceptionWhenInitialBalanceIsNegative() {
            assertThrows(InvalidAmountException.class,
                    () -> new AccountDomain(UUID.randomUUID(), OWNER_ID, new AccountNumber("001-123456"),
                            AccountType.CHECKING, new com.bancopago.backend.domain.account.vo.Money(
                                    new BigDecimal("-100"), Currency.COP), AccountStatus.ACTIVE));
        }
    }

    @Nested
    @DisplayName("Balance operations")
    class BalanceOperations {

        @Test
        @DisplayName("should deposit correctly")
        void shouldDepositCorrectly() {
            account.deposit(new BigDecimal("500.00"));
            assertEquals(new BigDecimal("500.00"), account.getBalance());
        }

        @Test
        @DisplayName("should withdraw correctly")
        void shouldWithdrawCorrectly() {
            account.deposit(new BigDecimal("1000.00"));
            account.withdraw(new BigDecimal("300.00"));
            assertEquals(new BigDecimal("700.00"), account.getBalance());
        }

        @Test
        @DisplayName("should throw exception when withdrawing with insufficient balance")
        void shouldThrowExceptionWhenWithdrawingWithInsufficientBalance() {
            assertThrows(InsufficientBalanceException.class,
                    () -> account.withdraw(new BigDecimal("100.00")));
        }

        @Test
        @DisplayName("should throw exception when depositing negative amount")
        void shouldThrowExceptionWhenDepositingNegativeAmount() {
            assertThrows(InvalidAmountException.class,
                    () -> account.deposit(new BigDecimal("-50")));
        }

        @Test
        @DisplayName("should throw exception when depositing zero amount")
        void shouldThrowExceptionWhenDepositingZeroAmount() {
            assertThrows(InvalidAmountException.class,
                    () -> account.deposit(BigDecimal.ZERO));
        }

        @Test
        @DisplayName("should throw exception when withdrawing negative amount")
        void shouldThrowExceptionWhenWithdrawingNegativeAmount() {
            assertThrows(InvalidAmountException.class,
                    () -> account.withdraw(new BigDecimal("-50")));
        }

        @Test
        @DisplayName("should not allow negative balance after multiple operations")
        void shouldNotAllowNegativeBalance() {
            account.deposit(new BigDecimal("100"));
            assertThrows(InsufficientBalanceException.class,
                    () -> account.withdraw(new BigDecimal("200")));
            assertEquals(new BigDecimal("100"), account.getBalance());
        }
    }

    @Nested
    @DisplayName("Status management")
    class StatusManagement {

        @Test
        @DisplayName("should block active account")
        void shouldBlockActiveAccount() {
            account.block();
            assertEquals(AccountStatus.BLOCKED, account.getStatus());
        }

        @Test
        @DisplayName("should unblock blocked account")
        void shouldUnblockBlockedAccount() {
            account.block();
            account.unblock();
            assertEquals(AccountStatus.ACTIVE, account.getStatus());
        }

        @Test
        @DisplayName("should close active account")
        void shouldCloseActiveAccount() {
            account.close();
            assertEquals(AccountStatus.INACTIVE, account.getStatus());
        }

        @Test
        @DisplayName("should close blocked account")
        void shouldCloseBlockedAccount() {
            account.block();
            account.close();
            assertEquals(AccountStatus.INACTIVE, account.getStatus());
        }

        @Test
        @DisplayName("should not block an already blocked account")
        void shouldNotBlockAlreadyBlockedAccount() {
            account.block();
            assertThrows(InvalidAccountStateException.class, () -> account.block());
        }

        @Test
        @DisplayName("should not unblock an account that is not blocked")
        void shouldNotUnblockNonBlockedAccount() {
            assertThrows(InvalidAccountStateException.class, () -> account.unblock());
        }

        @Test
        @DisplayName("should not close an already closed account")
        void shouldNotCloseAlreadyClosedAccount() {
            account.close();
            assertThrows(InvalidAccountStateException.class, () -> account.close());
        }

        @Test
        @DisplayName("should not unblock a closed account")
        void shouldNotUnblockClosedAccount() {
            account.close();
            assertThrows(InvalidAccountStateException.class, () -> account.unblock());
        }

        @Test
        @DisplayName("should not block a closed account")
        void shouldNotBlockClosedAccount() {
            account.close();
            assertThrows(InvalidAccountStateException.class, () -> account.block());
        }
    }

    @Nested
    @DisplayName("Operations on inactive accounts")
    class OperationsOnInactiveAccounts {

        @Test
        @DisplayName("should not deposit into blocked account")
        void shouldNotDepositIntoBlockedAccount() {
            account.block();
            assertThrows(AccountBlockedException.class,
                    () -> account.deposit(new BigDecimal("100")));
        }

        @Test
        @DisplayName("should not withdraw from blocked account")
        void shouldNotWithdrawFromBlockedAccount() {
            account.block();
            assertThrows(AccountBlockedException.class,
                    () -> account.withdraw(new BigDecimal("100")));
        }

        @Test
        @DisplayName("should not deposit into closed account")
        void shouldNotDepositIntoClosedAccount() {
            account.close();
            assertThrows(InvalidAccountStateException.class,
                    () -> account.deposit(new BigDecimal("100")));
        }

        @Test
        @DisplayName("should not withdraw from closed account")
        void shouldNotWithdrawFromClosedAccount() {
            account.close();
            assertThrows(InvalidAccountStateException.class,
                    () -> account.withdraw(new BigDecimal("100")));
        }
    }
}
