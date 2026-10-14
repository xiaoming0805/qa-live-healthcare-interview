package com.leansofx.qaserviceuser.exception;

/**
 * 查询不到患者时抛出，由 GlobalExceptionHandler 转换为 404。
 */
public class PatientNotFoundException extends RuntimeException {

    public PatientNotFoundException(String message) {
        super(message);
    }
}
