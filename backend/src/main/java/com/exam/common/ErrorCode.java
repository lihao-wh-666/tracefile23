package com.exam.common;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(200, "操作成功", "操作执行成功", ErrorCategory.SUCCESS),
    FAIL(500, "操作失败", "操作执行失败，请稍后重试", ErrorCategory.SYSTEM),
    BAD_REQUEST(400, "请求参数错误", "请求参数不合法，请检查后重试", ErrorCategory.PARAM),
    UNAUTHORIZED(401, "未授权，请先登录", "用户未登录或登录状态已过期", ErrorCategory.AUTH),
    FORBIDDEN(403, "无权限访问", "当前用户没有访问该资源的权限", ErrorCategory.AUTH),
    NOT_FOUND(404, "资源不存在", "请求的资源不存在或已被删除", ErrorCategory.RESOURCE),

    USER_NOT_FOUND(1001, "用户不存在", "该用户账号不存在", ErrorCategory.BUSINESS),
    USER_PASSWORD_ERROR(1002, "用户名或密码错误", "请检查用户名和密码是否正确", ErrorCategory.BUSINESS),
    USER_ALREADY_EXIST(1003, "用户已存在", "该用户名或邮箱已被注册", ErrorCategory.BUSINESS),
    USER_DISABLED(1004, "用户已被禁用", "该账号已被管理员禁用，请联系管理员", ErrorCategory.BUSINESS),
    USER_PASSWORD_SAME(1005, "新密码不能与旧密码相同", "请输入与旧密码不同的新密码", ErrorCategory.BUSINESS),
    USER_LOGIN_LOCKED(1006, "登录失败次数过多，账号已被锁定", "密码错误次数超过限制，账号已被临时锁定，请稍后再试", ErrorCategory.BUSINESS),

    TOKEN_INVALID(2001, "Token无效", "Token格式不正确或已被篡改", ErrorCategory.AUTH),
    TOKEN_EXPIRED(2002, "Token已过期", "登录状态已过期，请重新登录", ErrorCategory.AUTH),
    TOKEN_EMPTY(2003, "Token不能为空", "请求头中未包含Token信息", ErrorCategory.AUTH),

    EXAM_NOT_FOUND(3001, "考试不存在", "该考试信息不存在或已被删除", ErrorCategory.BUSINESS),
    EXAM_NOT_STARTED(3002, "考试未开始", "当前时间早于考试开始时间", ErrorCategory.BUSINESS),
    EXAM_ENDED(3003, "考试已结束", "当前时间已超过考试结束时间", ErrorCategory.BUSINESS),
    EXAM_ALREADY_TAKEN(3004, "您已参加过此次考试", "该考试只允许参加一次", ErrorCategory.BUSINESS),

    PAPER_NOT_FOUND(4001, "试卷不存在", "该试卷信息不存在或已被删除", ErrorCategory.BUSINESS),
    PAPER_NO_QUESTIONS(4002, "试卷没有题目", "当前试卷未配置任何题目", ErrorCategory.BUSINESS),
    PAPER_PUBLISHED(4003, "试卷已发布，无法修改", "已发布的试卷不能编辑或删除", ErrorCategory.BUSINESS),

    QUESTION_NOT_FOUND(5001, "题目不存在", "该题目信息不存在或已被删除", ErrorCategory.BUSINESS),
    QUESTION_TYPE_UNSUPPORTED(5002, "题目类型不支持", "该题目类型暂不支持", ErrorCategory.BUSINESS),

    SUBJECT_NOT_FOUND(6001, "学科不存在", "该学科信息不存在或已被删除", ErrorCategory.BUSINESS),
    SUBJECT_HAS_EXAM(6002, "学科下存在考试", "请先删除该学科下的所有考试", ErrorCategory.BUSINESS),

    PARAM_ERROR(7001, "参数错误", "请求参数不合法", ErrorCategory.PARAM),
    PARAM_EMPTY(7002, "参数不能为空", "必填参数缺失", ErrorCategory.PARAM),
    PARAM_FORMAT_ERROR(7003, "参数格式错误", "参数格式不符合要求", ErrorCategory.PARAM),
    PARAM_OUT_OF_RANGE(7004, "参数超出范围", "参数值超出允许的范围", ErrorCategory.PARAM),

    FILE_UPLOAD_ERROR(8001, "文件上传失败", "文件上传过程中发生错误", ErrorCategory.FILE),
    FILE_TYPE_ERROR(8002, "文件类型不支持", "请上传支持的文件格式", ErrorCategory.FILE),
    FILE_SIZE_ERROR(8003, "文件大小超出限制", "文件大小超过最大允许值", ErrorCategory.FILE),
    FILE_NOT_FOUND(8004, "文件不存在", "请求的文件不存在", ErrorCategory.FILE),
    FILE_READ_ERROR(8005, "文件读取失败", "文件读取过程中发生错误", ErrorCategory.FILE),

    DATABASE_ERROR(9001, "数据库操作错误", "数据库操作失败，请稍后重试", ErrorCategory.SYSTEM),
    SYSTEM_ERROR(9999, "系统异常", "系统内部错误，请联系管理员", ErrorCategory.SYSTEM);

    private final Integer code;
    private final String message;
    private final String detail;
    private final ErrorCategory category;

    ErrorCode(Integer code, String message, String detail, ErrorCategory category) {
        this.code = code;
        this.message = message;
        this.detail = detail;
        this.category = category;
    }

    @Getter
    public enum ErrorCategory {
        SUCCESS("success", "成功"),
        PARAM("param", "参数错误"),
        AUTH("auth", "认证授权错误"),
        BUSINESS("business", "业务错误"),
        SYSTEM("system", "系统错误"),
        RESOURCE("resource", "资源错误"),
        FILE("file", "文件操作错误");

        private final String code;
        private final String description;

        ErrorCategory(String code, String description) {
            this.code = code;
            this.description = description;
        }
    }
}
