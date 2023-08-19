package net.proselyte.webfluxsecurity.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.security.Principal;

/**
 * Класс расширяет стандартный Principal, добавляя к нему Id юзера.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomPrincipal implements Principal {
    private Long id; //id пользователя
    private String name; //имя пользователя
}
