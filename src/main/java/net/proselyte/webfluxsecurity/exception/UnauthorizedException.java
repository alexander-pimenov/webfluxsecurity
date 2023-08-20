package net.proselyte.webfluxsecurity.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Используется в error handler.
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends ApiException {
    /**
     * Конструктор только с message, а код стандартный PROSELYTE_UNAUTHORIZED
     *
     * @param message сообщение ошибки
     */
    public UnauthorizedException(String message) {
        super(message, "PROSELYTE_UNAUTHORIZED");
    }
}
