package net.proselyte.webfluxsecurity.exception;


import lombok.Getter;

/**
 * ApiException - дали такое название, т.к. будем его прокидывать наружу.
 * Этот exception мы сможем наследовать для всех своих кастомных исключений.
 * В этом классе мы просто создали нужную нам структуру, которую будем переиспользовать
 * потом.
 */
public class ApiException extends RuntimeException{

    /**
     * Это поле завели для удобства, т.к. с кодами проще работать.
     * По ним сразу понятно, что это за исключение.
     * И по кодам легче вести документацию, отдавать фронту.
     */
    @Getter
    protected String errorCode;

    public ApiException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
