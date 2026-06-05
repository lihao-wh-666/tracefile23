package com.exam.common;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权，请先登录"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USER_ALREADY_EXIST(1003, "用户已存在"),
    USER_DISABLED(1004, "用户已被禁用"),

    TOKEN_INVALID(2001, "Token无效"),
    TOKEN_EXPIRED(2002, "Token已过期"),
    TOKEN_EMPTY(2003, "Token不能为空"),

    EXAM_NOT_FOUND(3001, "考试不存在"),
    EXAM_NOT_STARTED(3002, "考试未开始"),
    EXAM_ENDED(3003, "考试已结束"),
    EXAM_ALREADY_TAKEN(3004, "您已参加过此次考试"),

    PAPER_NOT_FOUND(4001, "试卷不存在"),
    PAPER_NO_QUESTIONS(4002, "试卷没有题目"),

    QUESTION_NOT_FOUND(5001, "题目不存在"),

    SUBJECT_NOT_FOUND(6001, "学科不存在"),

    PARAM_ERROR(7001, "参数错误"),
    PARAM_EMPTY(7002, "参数不能为空"),

    FILE_UPLOAD_ERROR(8001, "文件上传失败"),
    FILE_TYPE_ERROR(8002, "文件类型不支持"),
    FILE_SIZE_ERROR(8003, "文件大小超出限制"),

    DATABASE_ERROR(9001, "数据库操作错误"),
    SYSTEM_ERROR(9999, "系统异常");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
