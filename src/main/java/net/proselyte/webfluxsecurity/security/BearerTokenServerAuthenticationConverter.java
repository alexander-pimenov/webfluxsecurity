package net.proselyte.webfluxsecurity.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * Класс-конвертер, который конвертирует запрос приходящий из вне
 * в аутентификацию.
 * Потом мы этот конвертер встраиваем в цепочку фильтров.
 */
@RequiredArgsConstructor
public class BearerTokenServerAuthenticationConverter implements ServerAuthenticationConverter {

    // передаем объект JwtHandler из вне через конструктор,
    // для удобства используем @RequiredArgsConstructor
    private final JwtHandler jwtHandler;
    /**
     * Токен хранится в заголовке в запросе "Bearer token"
     */
    private static final String BEARER_PREFIX = "Bearer ";
    /**
     * Функция для вытаскивания из запроса токена.
     * На вход приходит стока и на выход отдает Mono<String>.
     * Вытаскиваем substring (токен) после префикса Bearer.
     * Т.е. убираем префикс и остается только токен.
     */
    private static final Function<String, Mono<String>> getBearerValue =
            authValue -> Mono.justOrEmpty(authValue.substring(BEARER_PREFIX.length()));

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return extractHeader(exchange)
                //достаем токен из хедера
                .flatMap(getBearerValue)
                //проверяем токен на валидность
                .flatMap(jwtHandler::check)
                //трансформируем верификацию VerificationResult в аутентификацию
                .flatMap(UserAuthenticationBearer::create);
    }

    /**
     * Метод вытаскивает нужный хедер.
     *
     * @param exchange хедер
     * @return строка завернутая в Mono
     */
    private Mono<String> extractHeader(ServerWebExchange exchange) {
        return Mono.justOrEmpty(
                exchange.getRequest() //достаем реквест
                        .getHeaders() //из реквеста получаем хедеры
                        .getFirst(HttpHeaders.AUTHORIZATION) //получаем первый хедер по имени AUTHORIZATION
        );
    }
}
