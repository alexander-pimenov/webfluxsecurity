package net.proselyte.webfluxsecurity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import net.proselyte.webfluxsecurity.entity.UserRole;

import java.time.LocalDateTime;

/*@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class) -
 * это для формата полей для JSON, выбираем для них стиль SnakeCase для всех полей.
 * Конечно можно над каждым полем поставить @JsonProperty("first_name")? но полей много
 * и решили поставить для всего класса.*/
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserDto {
    private Long id;
    private String username;
    /*WRITE_ONLY - будем читать только при получении из вне, когда его создает пользователь*/
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    private UserRole role;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
