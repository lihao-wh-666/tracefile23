package com.exam.common;

import lombok.Getter;

import java.util.Map;

@Getter
public class ValidationException extends BaseException {

    private final Map<String, String> fieldErrors;

    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
        this.fieldErrors = null;
    }

    public ValidationException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
        this.fieldErrors = null;
    }

    public ValidationException(String message) {
        super(ErrorCode.PARAM_ERROR.getCode(), message);
        this.fieldErrors = null;
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super(ErrorCode.PARAM_ERROR.getCode(), message);
        this.fieldErrors = fieldErrors;
    }

    public ValidationException(Map<String, String> fieldErrors) {
        super(ErrorCode.PARAM_ERROR.getCode(), "参数校验失败");
        this.fieldErrors = fieldErrors;
    }

    public static ValidationException of(String message) {
        return new ValidationException(message);
    }

    public static ValidationException of(String message, Map<String, String> fieldErrors) {
        return new ValidationException(message, fieldErrors);
    }

    public static ValidationException of(Map<String, String> fieldErrors) {
        return new ValidationException(fieldErrors);
    }
}
