package com.bancopago.backend.infrastructure;

import com.bancopago.backend.crosscutting.exception.DomainException;
import com.bancopago.backend.domain.auth.exceptions.DuplicateUserEmailException;
import com.bancopago.backend.domain.auth.exceptions.InvalidCredentialsException;
import com.bancopago.backend.domain.auth.exceptions.UserNotFoundException;
import com.bancopago.backend.domain.account.exceptions.AccountBlockedException;
import com.bancopago.backend.domain.account.exceptions.AccountNotFoundException;
import com.bancopago.backend.domain.account.exceptions.DuplicateAccountException;
import com.bancopago.backend.domain.account.exceptions.DuplicateAccountTypeException;
import com.bancopago.backend.domain.account.exceptions.InsufficientBalanceException;
import com.bancopago.backend.domain.account.exceptions.InvalidAccountException;
import com.bancopago.backend.domain.account.exceptions.InvalidAccountStateException;
import com.bancopago.backend.domain.account.exceptions.InvalidAmountException;
import com.bancopago.backend.domain.account.exceptions.MaxAccountsExceededException;
import com.bancopago.backend.domain.person.exceptions.DuplicateDocumentException;
import com.bancopago.backend.domain.person.exceptions.DuplicateEmailException;
import com.bancopago.backend.domain.person.exceptions.InvalidPersonException;
import com.bancopago.backend.domain.person.exceptions.PersonNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidCredentialsException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUnauthorized(DomainException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(toError(ex)));
    }

    @ExceptionHandler({
            PersonNotFoundException.class,
            AccountNotFoundException.class,
            UserNotFoundException.class
    })
    public Mono<ResponseEntity<ErrorResponse>> handleNotFound(DomainException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(toError(ex)));
    }

    @ExceptionHandler({
            DuplicateDocumentException.class,
            DuplicateEmailException.class,
            DuplicateAccountException.class,
            DuplicateAccountTypeException.class,
            AccountBlockedException.class,
            MaxAccountsExceededException.class,
            DuplicateUserEmailException.class
    })
    public Mono<ResponseEntity<ErrorResponse>> handleConflict(DomainException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(toError(ex)));
    }

    @ExceptionHandler({
            InvalidAccountStateException.class,
            InvalidAccountException.class,
            InvalidPersonException.class,
            InvalidAmountException.class,
            InsufficientBalanceException.class
    })
    public Mono<ResponseEntity<ErrorResponse>> handleBadRequest(DomainException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(toError(ex)));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleWebExchangeBind(WebExchangeBindException ex) {
        return Mono.just(validationError(ex.getBindingResult()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        return Mono.just(validationError(ex.getBindingResult()));
    }

    @ExceptionHandler(DomainException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleDomainFallback(DomainException ex) {
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(toError(ex)));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("INTERNAL_ERROR", "An unexpected error occurred")));
    }

    private static ResponseEntity<ErrorResponse> validationError(BindingResult bindingResult) {
        List<String> messages = bindingResult.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        String message = messages.isEmpty() ? "Validation failed" : messages.getFirst();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("VALIDATION_ERROR", message, messages));
    }

    private static ErrorResponse toError(DomainException ex) {
        return ErrorResponse.of(ex.getCode(), ex.getUserMessage());
    }
}
