package net.proselyte.webfluxsecurity.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import net.proselyte.webfluxsecurity.entity.UserEntity;
import net.proselyte.webfluxsecurity.exception.AuthException;
import net.proselyte.webfluxsecurity.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityService {

    //инжектим два объекта UserService, PasswordEncoder
    private final UserService userService;
    private final PasswordEncoder passwordEncoder; //для работы с закодированным паролем

    /**
     * Секрет с помощью, которого подписывается токен
     */
    @Value("${jwt.secret}")
    private String secret;
    /**
     * Время валидности токена в секундах, чтоб было удобнее читать
     */
    @Value("${jwt.expiration}")
    private Integer expirationInSeconds;
    /**
     * Кто выдавал токен
     */
    @Value("${jwt.issuer}")
    private String issuer;

    /**
     * Метод генерации токена.
     * Токен состоит из трех частей: заголовок, пейлоад, подпись.
     * <p>
     * С помощью перегрузок метода накинем нужные нам данные в расширенный токен.
     * <p>
     * Для создания токена применяется конструктор JwtSecurityToken.
     * Одним из параметров служит список объектов Claim.
     * Объекты Claim служат для хранения некоторых данных о пользователе,
     * описывают пользователя. Затем эти данные можно применять для
     * аутентификации.
     *
     * @param user пользователь из БД
     * @return токен с деталями (расширенный токен)
     */
    private TokenDetails generateToken(UserEntity user) {

        //создаем claims
        Map<String, Object> claims = new HashMap<>() {{
            put("role", user.getRole()); //хотим в claims передать роль
            put("username", user.getUsername()); //хотим в claims передать username
        }};
        return generateToken(claims, user.getId().toString());
    }

    /**
     * Перегруженный метод generateToken.
     * В нем создаем дату экспирации токена.
     *
     * @param claims  claims (доп данные пользователя)
     * @param subject кому предназначен токен (id пользователя из БД)
     * @return расширенный токен
     */
    private TokenDetails generateToken(Map<String, Object> claims, String subject) {
        //преобразуем длительность экспирации токена в миллисекунды
        long expirationTimeInMillis = expirationInSeconds * 1000L;
        //создаем дату экспирации токена типа Date
        long createdDate = new Date().getTime();
        Date expirationDate = new Date(createdDate + expirationTimeInMillis);

        return generateToken(expirationDate, claims, subject);
    }

    /**
     * Перегрузка метода generateToken
     *
     * @param expirationDate время действия токена в секундах
     * @param claims         claims (доп данные пользователя)
     * @param subject        кому предназначен токен (id пользователя из БД)
     * @return расширенный токен
     */
    private TokenDetails generateToken(Date expirationDate, Map<String, Object> claims, String subject) {
        Date createdDate = new Date(); //дата создания токена
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuer(issuer) //издатель токена
                .setSubject(subject) //для кого токен - id пользователя
                .setIssuedAt(createdDate) //дата выдачи/создания токена
                .setId(UUID.randomUUID().toString())
                .setExpiration(expirationDate) //дата срока действия токена
                //тут подписываем токен
                .signWith(
                        SignatureAlgorithm.HS256, //алгоритм подписи токена
                        Base64.getEncoder().encodeToString(secret.getBytes())) //секрет для подписи закодируем
                .compact();

        //возвращаем расширенный токен, накинув в него даты выдачи и экспирации
        return TokenDetails.builder()
                .token(token)
                .issuedAt(createdDate)     //дата выдачи/создания токена
                .expiresAt(expirationDate) //дата срока действия токена
                .build();
    }

    /**
     * Метод отдает - расширенный токен.
     * Находим пользователя в БД, сверяем его пароль с тем, что пришел,
     * то отдаем токен. Т.е. это говорит, что пользователь валидный.
     *
     * @param username имя пользователя (логин)
     * @param password пароль пользователя
     * @return расширенный токен
     */
    public Mono<TokenDetails> authenticate(String username, String password) {
        return userService.getUserByUsername(username) //найди пользователя по его username
                //если пользователь есть то делай следующее:
                .flatMap(user -> {
                    //если пользователь не enabled, то кидаем ошибку
                    if (!user.isEnabled()) {
                        return Mono.error(new AuthException("Account disabled", "PROSELYTE_USER_ACCOUNT_DISABLED"));
                    }
                    //если пароли не совпадают, то кинь ошибку
                    if (!passwordEncoder.matches(password, user.getPassword())) {
                        return Mono.error(new AuthException("Invalid password", "PROSELYTE_INVALID_PASSWORD"));
                    }
                    //если все хорошо, то вернем расширенный токен, добавив в него id пользователя,
                    //т.к. не всегда удобно парсить детали из токена
                    return Mono.just(generateToken(user).toBuilder()
                            .userId(user.getId())
                            .build());
                })
                //если нет пользователя, то кинь ошибку
                .switchIfEmpty(Mono.error(new AuthException("Invalid username", "PROSELYTE_INVALID_USERNAME")));
    }
}
