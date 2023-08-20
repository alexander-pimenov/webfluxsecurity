package net.proselyte.webfluxsecurity.config;

import lombok.extern.slf4j.Slf4j;
import net.proselyte.webfluxsecurity.security.AuthenticationManager;
import net.proselyte.webfluxsecurity.security.BearerTokenServerAuthenticationConverter;
import net.proselyte.webfluxsecurity.security.JwtHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
@EnableReactiveMethodSecurity
public class WebSecurityConfig {

    @Value("${jwt.secret}")
    private String secret;

    /**
     * Публичные роуты, к которым имеем доступ:
     * - эндпоинт регистрации
     * - эндпоинт аутентификации
     */
    private final String[] publicRoutes = {"/api/v1/auth/register", "/api/v1/auth/login"};

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, AuthenticationManager authenticationManager) {
        return http
                //отключаем csrf
                .csrf().disable()
                .authorizeExchange()
                //говорим, что разрешаем OPTIONS запросы всем
                .pathMatchers(HttpMethod.OPTIONS)
                .permitAll()
                //говорим, что публичные запросы, указанные в массиве, разрешаем всем
                .pathMatchers(publicRoutes)
                .permitAll()
                //любой запрос должен быть аутентифицирован
                .anyExchange()
                .authenticated()
                .and()
                //обработаем ошибки
                .exceptionHandling()
                // - если ошибка на энтрипоинте
                .authenticationEntryPoint((swe, e) -> {
                    log.error("IN securityWebFilterChain - unauthorized error: {}", e.getMessage());
                    return Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED));
                })
                // - если доступ не разрешен
                .accessDeniedHandler((swe, e) -> {
                    log.error("IN securityWebFilterChain - access denied: {}", e.getMessage());
                    return Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN));
                })
                .and()
                //добавляем фильтр для процесса аутентификации и его порядок - аутентификация
                .addFilterAt(bearerAuthenticationFilter(authenticationManager), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    /**
     * Реализуем свой AuthenticationWebFilter и добавляем этот фильтр в цепочку
     * для процесса аутентификации.
     *
     * @param authenticationManager authenticationManager
     * @return AuthenticationWebFilter
     */
    private AuthenticationWebFilter bearerAuthenticationFilter(AuthenticationManager authenticationManager) {
        //создаем AuthenticationWebFilter
        AuthenticationWebFilter bearerAuthenticationFilter = new AuthenticationWebFilter(authenticationManager);

        //создаваемый фильтр принимает в себя конвертер BearerTokenServerAuthenticationConverter
        bearerAuthenticationFilter.setServerAuthenticationConverter(
                new BearerTokenServerAuthenticationConverter(
                        new JwtHandler(secret) //хендлер с секретом
                )
        );
        //говорим, что это фильтр применяется для всех входящих запросов, которые у нас есть.
        bearerAuthenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/**"));

        return bearerAuthenticationFilter;
    }
}
