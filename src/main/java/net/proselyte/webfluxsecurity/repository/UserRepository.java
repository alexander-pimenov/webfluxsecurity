package net.proselyte.webfluxsecurity.repository;

import net.proselyte.webfluxsecurity.entity.UserEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

/*Реактивный репозиторий.
* Mono - реактивная надстройка над возвращаемым типом.*/
public interface UserRepository extends R2dbcRepository<UserEntity, Long> {

    Mono<UserEntity> findByUsername(String username);
}
