package net.proselyte.webfluxsecurity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.proselyte.webfluxsecurity.entity.UserEntity;
import net.proselyte.webfluxsecurity.entity.UserRole;
import net.proselyte.webfluxsecurity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    //инжектим два объекта UserRepository, PasswordEncoder
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Метод сохранения пользователя в БД.
     *
     * @param user объект пользователя, как энтити
     * @return энтити пользователя
     */
    public Mono<UserEntity> registerUser(UserEntity user) {
        //сохраняем пользователя в БД с закодированным паролем и ролью USER
        return userRepository.save(
                        user.toBuilder()
                                .password(passwordEncoder.encode(user.getPassword()))
                                .role(UserRole.USER)
                                .enabled(true)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build()
                )
                //в случае успеха - выводим сообщение в лог
                .doOnSuccess(u -> {
                    log.info("IN registerUser - user: {} created", u);
                });
    }

    /**
     * Получение данных по пользователю.
     *
     * @param id id пользователя
     * @return энтити пользователя завернутое в Mono
     */
    public Mono<UserEntity> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Получение данных по пользователю.
     *
     * @param username id username
     * @return энтити пользователя завернутое в Mono
     */
    public Mono<UserEntity> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
