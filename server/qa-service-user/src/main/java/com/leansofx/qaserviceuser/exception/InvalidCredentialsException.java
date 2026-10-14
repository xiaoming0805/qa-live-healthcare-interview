package com.leansofx.qaserviceuser.exception;

/**
 * 用户名或密码不正确时抛出，由 GlobalExceptionHandler 转换为 401。
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
