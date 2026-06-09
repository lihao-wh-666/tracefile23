package com.exam.common;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Data
public class Result<T> {

    public static final Integer SUCCESS_CODE = 200;
    public static final Integer FAIL_CODE = 500;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Integer code;
    private String msg;
    private T data;
    private String traceId;
    private String timestamp;
    private String detail;

    public static <T> Result<T> ok() {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMsg("操作成功");
        result.setTimestamp(LocalDateTime.now().format(FORMATTER));
        return result;
    }

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMsg("操作成功");
        result.setData(data);
        result.setTimestamp(LocalDateTime.now().format(FORMATTER));
        return result;
    }

    public static <T> Result<T> ok(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMsg(msg);
        result.setData(data);
        result.setTimestamp(LocalDateTime.now().format(FORMATTER));
        return result;
    }

    public static <T> Result<T> fail() {
        Result<T> result = new Result<>();
        result.setCode(FAIL_CODE);
        result.setMsg("操作失败");
        result.setTraceId(generateTraceId());
        result.setTimestamp(LocalDateTime.now().format(FORMATTER));
        return result;
    }

    public static <T> Result<T> fail(String msg) {
        Result<T> result = new Result<>();
        result.setCode(FAIL_CODE);
        result.setMsg(msg);
        result.setTraceId(generateTraceId());
        result.setTimestamp(LocalDateTime.now().format(FORMATTER));
        return result;
    }

    public static <T> Result<T> fail(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setTraceId(generateTraceId());
        result.setTimestamp(LocalDateTime.now().format(FORMATTER));
        return result;
    }

    public static <T> Result<T> fail(Integer code, String msg, String detail) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        result.setDetail(detail);
        result.setTraceId(generateTraceId());
        result.setTimestamp(LocalDateTime.now().format(FORMATTER));
        return result;
    }

    public static <T> Result<T> fail(ErrorCode errorCode) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMsg(errorCode.getMessage());
        result.setDetail(errorCode.getDetail());
        result.setTraceId(generateTraceId());
        result.setTimestamp(LocalDateTime.now().format(FORMATTER));
        return result;
    }

    public static <T> Result<T> fail(ErrorCode errorCode, String detail) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMsg(errorCode.getMessage());
        result.setDetail(detail);
        result.setTraceId(generateTraceId());
        result.setTimestamp(LocalDateTime.now().format(FORMATTER));
        return result;
    }

    public static <T> Result<T> fail(BaseException e) {
        Result<T> result = new Result<>();
        result.setCode(e.getCode());
        result.setMsg(e.getMessage());
        result.setDetail(e.getDetail());
        result.setTraceId(e.getTraceId() != null ? e.getTraceId() : generateTraceId());
        result.setTimestamp(LocalDateTime.now().format(FORMATTER));
        return result;
    }

    public Result<T> withTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    private static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
