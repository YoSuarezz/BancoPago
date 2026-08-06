package com.bancopago.backend.infrastructure.primaryadapters.adapter.response;

import com.bancopago.backend.application.model.PageResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.util.List;

public final class HttpResponses {

    private HttpResponses() {
    }

    public static <T> Mono<ResponseEntity<ApiResponse<T>>> created(Mono<T> body, String message) {
        return body.map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(dto, message)));
    }

    public static <T> Mono<ResponseEntity<ApiResponse<T>>> ok(Mono<T> body) {
        return body.map(dto -> ResponseEntity.ok(ApiResponse.of(dto)));
    }

    public static <T> Mono<ResponseEntity<ApiResponse<T>>> ok(Mono<T> body, String message) {
        return body.map(dto -> ResponseEntity.ok(ApiResponse.of(dto, message)));
    }

    public static <T> Mono<ResponseEntity<ApiResponse<T>>> okList(Mono<List<T>> body) {
        return body.map(list -> ResponseEntity.ok(ApiResponse.of(list)));
    }

    /**
     * Traduce PageResult (aplicación) → PageResponse (infra HTTP) y lo envuelve en 200 OK.
     */
    public static <T> Mono<ResponseEntity<PageResponse<T>>> okPage(Mono<PageResult<T>> body) {
        return body.map(PageResponse::from).map(ResponseEntity::ok);
    }
}
