package com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.config;

import com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.entity.AccountEntity;
import com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.entity.PersonEntity;
import com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.entity.TransferEntity;
import com.bancopago.backend.infrastructure.secondaryadapters.r2dbc.entity.UserEntity;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.mapping.event.AfterConvertCallback;
import org.springframework.data.relational.core.sql.SqlIdentifier;
import reactor.core.publisher.Mono;

@Configuration
public class R2dbcPersistableConfig {

    @Bean
    AfterConvertCallback<PersonEntity> personAfterConvertCallback() {
        return new AfterConvertCallback<>() {
            @Override
            public Publisher<PersonEntity> onAfterConvert(PersonEntity entity, SqlIdentifier table) {
                entity.markPersisted();
                return Mono.just(entity);
            }
        };
    }

    @Bean
    AfterConvertCallback<AccountEntity> accountAfterConvertCallback() {
        return new AfterConvertCallback<>() {
            @Override
            public Publisher<AccountEntity> onAfterConvert(AccountEntity entity, SqlIdentifier table) {
                entity.markPersisted();
                return Mono.just(entity);
            }
        };
    }

    @Bean
    AfterConvertCallback<UserEntity> userAfterConvertCallback() {
        return new AfterConvertCallback<>() {
            @Override
            public Publisher<UserEntity> onAfterConvert(UserEntity entity, SqlIdentifier table) {
                entity.markPersisted();
                return Mono.just(entity);
            }
        };
    }

    @Bean
    AfterConvertCallback<TransferEntity> transferAfterConvertCallback() {
        return new AfterConvertCallback<>() {
            @Override
            public Publisher<TransferEntity> onAfterConvert(TransferEntity entity, SqlIdentifier table) {
                entity.markPersisted();
                return Mono.just(entity);
            }
        };
    }
}
