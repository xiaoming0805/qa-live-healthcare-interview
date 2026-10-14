package com.leansofx.qaserviceuser.exception;

/**
 * 注册时用户名冲突，由 GlobalExceptionHandler 转换为 409。
 */
public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}
