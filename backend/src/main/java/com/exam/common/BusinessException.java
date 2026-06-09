package com.exam.common;

import lombok.Getter;

@Getter
public class BusinessException extends BaseException {

    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    public BusinessException(Integer code, String message) {
        super(code, message);
    }

    public BusinessException(Integer code, String message, String detail) {
        super(code, message, detail);
    }

    public BusinessException(String message) {
        super(message);
    }

    public static BusinessException of(ErrorCode errorCode) {
        return new BusinessException(errorCode);
    }

    public static BusinessException of(ErrorCode errorCode, String detail) {
        return new BusinessException(errorCode, detail);
    }

    public static BusinessException of(String message) {
        return new BusinessException(message);
    }

    public static BusinessException of(Integer code, String message) {
        return new BusinessException(code, message);
    }
}
