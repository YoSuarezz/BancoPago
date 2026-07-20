package com.bancopago.backend.domain;

@FunctionalInterface
public interface DomainRule<T> {
    void validate(T data);
}
