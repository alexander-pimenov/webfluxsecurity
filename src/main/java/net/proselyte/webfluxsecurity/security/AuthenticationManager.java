package net.proselyte.webfluxsecurity.security;

import lombok.RequiredArgsConstructor;
import net.proselyte.webfluxsecurity.entity.UserEntity;
import net.proselyte.webfluxsecurity.exception.UnauthorizedException;
import net.proselyte.webfluxsecurity.service.UserService;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Класс-менеджер аутентификации приложения.
 */
@Component
@RequiredArgsConstructor
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final UserService userService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        //достаем из Authentication принципал (CustomPrincipal)
        CustomPrincipal principal = (CustomPrincipal) authentication.getPrincipal();
        return userService.getUserById(principal.getId())
                //проверяем, что пользователь активный
                .filter(UserEntity::isEnabled)
                //если нет то кидаем исключение
                .switchIfEmpty(Mono.error(new UnauthorizedException("User disabled")))
                //если всё ОК, то мапим всё на аутентификацию
                .map(user -> authentication);
    }
}
