package net.proselyte.webfluxsecurity.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Класс для трансформации проверки верификации токена VerificationResult
 * в аутентификацию Authentication.
 */
public class UserAuthenticationBearer {

    public static Mono<Authentication> create(JwtHandler.VerificationResult verificationResult) {
        //вытягиваем клеймсы
        Claims claims = verificationResult.claims;

        //из клеймсов достаем Subject - это id пользователя
        String subject = claims.getSubject();
        //из клеймсов достаем role (мы передавили туда её ранее вкладывая через мапу)
        String role = claims.get("role", String.class);
        //из клеймсов достаем username (мы передавили туда его ранее вкладывая через мапу)
        String username = claims.get("username", String.class);

        //транслируем role в Authority
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

        //создаем принципал
        Long principalId = Long.parseLong(subject);
        CustomPrincipal principal = new CustomPrincipal(principalId, username);

        //возвращаем аутентификацию, завернутую в Mono
        return Mono.justOrEmpty(new UsernamePasswordAuthenticationToken(principal, null, authorities));
    }
}
