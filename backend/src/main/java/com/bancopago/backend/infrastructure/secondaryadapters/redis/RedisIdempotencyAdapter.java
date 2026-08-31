package com.bancopago.backend.infrastructure.secondaryadapters.redis;

import com.bancopago.backend.application.secondaryports.service.IdempotencyService;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Component
public class RedisIdempotencyAdapter implements IdempotencyService {

    private static final String KEY_PREFIX = "idempotency:transfer:";

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public RedisIdempotencyAdapter(ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<Boolean> exists(String key) {
        return redisTemplate.hasKey(KEY_PREFIX + key);
    }

    @Override
    public Mono<Void> store(String key, UUID transferId, Duration ttl) {
        return redisTemplate.opsForValue()
                .set(KEY_PREFIX + key, transferId.toString(), ttl)
                .then();
    }

    @Override
    public Mono<UUID> getTransferId(String key) {
        return redisTemplate.opsForValue()
                .get(KEY_PREFIX + key)
                .map(UUID::fromString);
    }
}
