package net.proselyte.webfluxsecurity.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Класс расширяющий токен. Надстройка над токеном.
 * Добавили еще
 * Long userId - id пользователя
 * Date issuedAt - когда выпустили токен
 * Date expiresAt - когда притухнет токен
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TokenDetails {
    private Long userId;
    private String token;
    private Date issuedAt;
    private Date expiresAt;
}
