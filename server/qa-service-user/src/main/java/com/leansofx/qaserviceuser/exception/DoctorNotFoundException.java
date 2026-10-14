package com.leansofx.qaserviceuser.exception;

/**
 * 查询不到医生时抛出，由 GlobalExceptionHandler 转换为 404。
 */
public class DoctorNotFoundException extends RuntimeException {

    public DoctorNotFoundException(String message) {
        super(message);
    }
}
