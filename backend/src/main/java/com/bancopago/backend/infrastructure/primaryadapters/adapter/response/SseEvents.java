package com.bancopago.backend.infrastructure.primaryadapters.adapter.response;

import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Flux;

public final class SseEvents {

    public static final String BALANCE = "balance";

    private SseEvents() {
    }

    public static <T> ServerSentEvent<T> of(String eventName, T data) {
        return ServerSentEvent.<T>builder()
                .event(eventName)
                .data(data)
                .build();
    }

    public static <T> Flux<ServerSentEvent<T>> map(Flux<T> source, String eventName) {
        return source.map(data -> of(eventName, data));
    }
}
