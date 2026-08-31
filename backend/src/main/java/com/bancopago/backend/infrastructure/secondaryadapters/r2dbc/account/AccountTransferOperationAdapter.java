package com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.account;

import com.bancopago.backend.application.secondaryports.repository.AccountRepository;
import com.bancopago.backend.application.secondaryports.service.AccountTransferOperationService;
import com.bancopago.backend.domain.transfer.TransferError;
import com.bancopago.backend.domain.transfer.exceptions.InvalidTransferException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Component
public class AccountTransferOperationAdapter implements AccountTransferOperationService {

    private final AccountRepository accountRepository;

    public AccountTransferOperationAdapter(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Mono<Void> executeTransfer(String sourceAccountNumber, String targetAccountNumber, BigDecimal amount) {
        return debit(sourceAccountNumber, amount)
                .then(credit(targetAccountNumber, amount));
    }

    private Mono<Void> debit(String accountNumber, BigDecimal amount) {
        return accountRepository.findAccountByNumber(accountNumber)
                .switchIfEmpty(Mono.error(InvalidTransferException.create(
                        TransferError.SOURCE_NOT_FOUND, accountNumber)))
                .flatMap(account -> {
                    account.withdraw(amount);
                    return accountRepository.saveAccount(account);
                })
                .then();
    }

    private Mono<Void> credit(String accountNumber, BigDecimal amount) {
        return accountRepository.findAccountByNumber(accountNumber)
                .switchIfEmpty(Mono.error(InvalidTransferException.create(
                        TransferError.TARGET_NOT_FOUND, accountNumber)))
                .flatMap(account -> {
                    account.deposit(amount);
                    return accountRepository.saveAccount(account);
                })
                .then();
    }
}
