package com.bancopago.backend.application.secondaryports.service;

import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface AccountTransferOperationService {

    Mono<Void> executeTransfer(String sourceAccountNumber, String targetAccountNumber, BigDecimal amount);
}
