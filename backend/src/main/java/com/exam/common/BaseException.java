package com.exam.common;

import lombok.Getter;

@Getter
public class BaseException extends RuntimeException {

    private final Integer code;
    private final String message;
    private final String detail;
    private String traceId;

    public BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.detail = errorCode.getDetail();
    }

    public BaseException(ErrorCode errorCode, String detail) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
        this.detail = detail;
    }

    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
        this.detail = null;
    }

    public BaseException(Integer code, String message, String detail) {
        super(message);
        this.code = code;
        this.message = message;
        this.detail = detail;
    }

    public BaseException(String message) {
        super(message);
        this.code = ErrorCode.FAIL.getCode();
        this.message = message;
        this.detail = null;
    }

    public BaseException setTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }
}
