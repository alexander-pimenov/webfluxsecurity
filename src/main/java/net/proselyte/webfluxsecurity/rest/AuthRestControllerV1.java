package net.proselyte.webfluxsecurity.rest;

import lombok.RequiredArgsConstructor;
import net.proselyte.webfluxsecurity.dto.AuthRequestDto;
import net.proselyte.webfluxsecurity.dto.AuthResponseDto;
import net.proselyte.webfluxsecurity.dto.UserDto;
import net.proselyte.webfluxsecurity.entity.UserEntity;
import net.proselyte.webfluxsecurity.mapper.UserMapper;
import net.proselyte.webfluxsecurity.security.CustomPrincipal;
import net.proselyte.webfluxsecurity.security.SecurityService;
import net.proselyte.webfluxsecurity.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthRestControllerV1 {
    //инжектим три объекта SecurityService, UserService, UserMapper
    private final SecurityService securityService;
    private final UserService userService;
    private final UserMapper userMapper;

    /**
     * Метод регистрации пользователя.
     *
     * @param dto dto входящего пользователя
     * @return зарегистрированный пользователь завернутый в Mono
     */
    @PostMapping("/register")
    public Mono<UserDto> register(@RequestBody UserDto dto) {
        //создаем объект энтити для дальнейшего сохранения в БД, используя маппер и метод map
        UserEntity entity = userMapper.map(dto);
        //сохраняем энтити в БД и возвращаем в ответе dto пользователя
        return userService.registerUser(entity)
                .map(userMapper::map);
    }

    /**
     * Метод вход пользователя по логину.
     *
     * @param dto данные пользователя username и password
     * @return dto аутентификации пользователя
     */

    @PostMapping("/login")
    public Mono<AuthResponseDto> login(@RequestBody AuthRequestDto dto) {
        //просим securityService аутентифицировать пользователя на основании
        //его username и password
        return securityService.authenticate(dto.getUsername(), dto.getPassword())
                //если всё ОК, то возвращается расширенный токен
                //и мы из него создаем dto аутентификации пользователя для ответа
                .flatMap(tokenDetails -> Mono.just(
                        AuthResponseDto.builder()
                                .userId(tokenDetails.getUserId())
                                .token(tokenDetails.getToken())
                                .issuedAt(tokenDetails.getIssuedAt())
                                .expiresAt(tokenDetails.getExpiresAt())
                                .build()
                ));
    }

    /**
     * Метод отдающий инфо по пользователю.
     *
     * @param authentication аутентификация из контекста (поэтому не нужно добавлять @RequestBody)
     * @return dto пользователя обернутый в Mono
     */
    @GetMapping("/info")
    public Mono<UserDto> getUserInfo(Authentication authentication) {
        CustomPrincipal customPrincipal = (CustomPrincipal) authentication.getPrincipal();

        return userService.getUserById(customPrincipal.getId())
                .map(userMapper::map);
    }
}
