package com.exam.common;

import lombok.Data;

@Data
public class Result<T> {

    public static final Integer SUCCESS_CODE = 200;
    public static final Integer FAIL_CODE = 500;

    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> ok() {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMsg("操作成功");
        return result;
    }

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMsg("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> ok(String msg, T data) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> fail() {
        Result<T> result = new Result<>();
        result.setCode(FAIL_CODE);
        result.setMsg("操作失败");
        return result;
    }

    public static <T> Result<T> fail(String msg) {
        Result<T> result = new Result<>();
        result.setCode(FAIL_CODE);
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> fail(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }
}
