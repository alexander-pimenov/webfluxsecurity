package net.proselyte.webfluxsecurity.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import net.proselyte.webfluxsecurity.exception.AuthException;
import net.proselyte.webfluxsecurity.exception.UnauthorizedException;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Date;

/**
 * Класс для проверки валидности токена.
 * Обрабатывает токен и возвращает результат проверки.
 * Для результата проверки вместо boolean, проще вернуть
 * кокой-то результат, например, токен и еще накинуть claims,
 * чтоб можно было получить доступ в будущем. Поэтому создали
 * внутренний статический класс VerificationResult.
 * <p>
 * JwtHandler можно сделать Component, чтоб инжектить secret, но т.к.
 * класс простой (в нем нет сложной логики) и чтоб потом не было циклических
 * зависимостей, не будем делать его Component, а просто будем в него
 * передавать secret снаружи через конструктор.
 */
public class JwtHandler {

    /**
     * Ключ для распарсивания токена.
     * Секрет, с помощью которого подписывали токен.
     */
    private final String secret;

    public JwtHandler(String secret) {
        this.secret = secret;
    }

    /**
     * Метод проверки валидности токена.
     * Проверяем подпись и дату экспирации.
     *
     * @param accessToken токен
     * @return объект верификации завернутый в Mono
     */
    public Mono<VerificationResult> check(String accessToken) {
        //отдаем объект верификации завернув его в Mono, а если
        //ошибка то прокидываем exception
        return Mono.just(verify(accessToken))
                .onErrorResume(e -> Mono.error(new UnauthorizedException(e.getMessage())));
    }

    /**
     * Метод возвращающий результат верификации
     * в нем проверяем подпись и дату экспирации.
     *
     * @param token токен
     * @return объект-обертку для результата VerificationResult
     */
    private VerificationResult verify(String token) {
        Claims claims = getClaimsFromToken(token);
        //достаем из клеймсов дату экспирации
        final Date expirationDate = claims.getExpiration();

        //проверяем дату экспирации токена
        if (expirationDate.before(new Date())) {

            // не кидаем кастомный exception типа AuthException, т.к. в методе check()
            // он и так будет перехвачен и завернут в UnauthorizedException, в котором
            // уже будет код PROSELYTE_TOKEN_EXPIRED, а кидаем обычный
            // RuntimeException("Token expired").
            // throw new AuthException("Token expired", "PROSELYTE_TOKEN_EXPIRED");

            throw new RuntimeException("Token expired");
        }

        return new VerificationResult(claims, token);
    }

    /**
     * Получает из токена клеймсы.
     *
     * @param token токен
     * @return claims
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(
                        //передаем секрет с помощью которого подписывали токен
                        Base64.getEncoder()
                                .encodeToString(secret.getBytes())
                )
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Класс-обертка для возвращаемого результата.
     * В нем есть нужные нам поля.
     */
    public static class VerificationResult {
        public Claims claims;
        public String token;

        public VerificationResult(Claims claims, String token) {
            this.claims = claims;
            this.token = token;
        }
    }
}
