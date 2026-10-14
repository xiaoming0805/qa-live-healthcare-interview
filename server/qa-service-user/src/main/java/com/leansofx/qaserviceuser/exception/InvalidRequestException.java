package com.leansofx.qaserviceuser.exception;

/**
 * 请求参数校验失败，由 GlobalExceptionHandler 转换为 400。
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
