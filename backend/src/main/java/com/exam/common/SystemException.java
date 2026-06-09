package com.exam.common;

import lombok.Getter;

@Getter
public class SystemException extends BaseException {

    public SystemException(ErrorCode errorCode) {
        super(errorCode);
    }

    public SystemException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public SystemException(Integer code, String message) {
        super(code, message);
    }

    public SystemException(Integer code, String message, String detail) {
        super(code, message, detail);
    }

    public SystemException(String message) {
        super(ErrorCode.SYSTEM_ERROR.getCode(), message);
    }

    public SystemException(String message, Throwable cause) {
        super(ErrorCode.SYSTEM_ERROR.getCode(), message);
        this.initCause(cause);
    }

    public static SystemException of(ErrorCode errorCode) {
        return new SystemException(errorCode);
    }

    public static SystemException of(ErrorCode errorCode, String detail) {
        return new SystemException(errorCode, detail);
    }

    public static SystemException of(String message) {
        return new SystemException(message);
    }

    public static SystemException of(String message, Throwable cause) {
        return new SystemException(message, cause);
    }
}
