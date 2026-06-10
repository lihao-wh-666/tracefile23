package com.exam.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private String getTraceId() {
        return TraceIdContext.getTraceId();
    }

    private <T> Result<T> buildResult(Result<T> result) {
        String traceId = getTraceId();
        if (traceId != null && result.getTraceId() == null) {
            result.setTraceId(traceId);
        }
        return result;
    }

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(BaseException.class)
    public Result handleBaseException(BaseException e) {
        String traceId = getTraceId();
        e.setTraceId(traceId);
        if (e instanceof BusinessException) {
            log.warn("业务异常: traceId={}, code={}, message={}, detail={}",
                    traceId, e.getCode(), e.getMessage(), e.getDetail());
        } else if (e instanceof ValidationException) {
            log.warn("参数校验异常: traceId={}, code={}, message={}, detail={}",
                    traceId, e.getCode(), e.getMessage(), e.getDetail());
        } else if (e instanceof SystemException) {
            log.error("系统异常: traceId={}, code={}, message={}, detail={}",
                    traceId, e.getCode(), e.getMessage(), e.getDetail(), e);
        } else {
            log.error("基础异常: traceId={}, code={}, message={}, detail={}",
                    traceId, e.getCode(), e.getMessage(), e.getDetail(), e);
        }
        return buildResult(Result.fail(e));
    }

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(BusinessException.class)
    public Result handleBusinessException(BusinessException e) {
        String traceId = getTraceId();
        e.setTraceId(traceId);
        log.warn("业务异常: traceId={}, code={}, message={}, detail={}",
                traceId, e.getCode(), e.getMessage(), e.getDetail());
        return buildResult(Result.fail(e));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @ExceptionHandler(ValidationException.class)
    public Result handleValidationException(ValidationException e) {
        String traceId = getTraceId();
        e.setTraceId(traceId);
        log.warn("参数校验异常: traceId={}, code={}, message={}, detail={}",
                traceId, e.getCode(), e.getMessage(), e.getDetail());
        Result result = Result.fail(e);
        if (e.getFieldErrors() != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("fieldErrors", e.getFieldErrors());
            result.setData(data);
        }
        return buildResult(result);
    }

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(SystemException.class)
    public Result handleSystemException(SystemException e) {
        String traceId = getTraceId();
        e.setTraceId(traceId);
        log.error("系统异常: traceId={}, code={}, message={}, detail={}",
                traceId, e.getCode(), e.getMessage(), e.getDetail(), e);
        return buildResult(Result.fail(e));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "校验失败",
                        (a, b) -> a
                ));
        String message = fieldErrors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        String traceId = getTraceId();
        log.warn("参数校验异常: traceId={}, message={}, fieldErrors={}", traceId, message, fieldErrors);
        ValidationException ve = new ValidationException(message, fieldErrors);
        ve.setTraceId(traceId);
        Result result = Result.fail(ve);
        Map<String, Object> data = new HashMap<>();
        data.put("fieldErrors", fieldErrors);
        result.setData(data);
        return buildResult(result);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @ExceptionHandler(BindException.class)
    public Result handleBindException(BindException e) {
        Map<String, String> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "校验失败",
                        (a, b) -> a
                ));
        String message = fieldErrors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数绑定失败");
        String traceId = getTraceId();
        log.warn("参数绑定异常: traceId={}, message={}, fieldErrors={}", traceId, message, fieldErrors);
        ValidationException ve = new ValidationException(message, fieldErrors);
        ve.setTraceId(traceId);
        Result result = Result.fail(ve);
        Map<String, Object> data = new HashMap<>();
        data.put("fieldErrors", fieldErrors);
        result.setData(data);
        return buildResult(result);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @ExceptionHandler(ConstraintViolationException.class)
    public Result handleConstraintViolationException(ConstraintViolationException e) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            String field = violation.getPropertyPath() != null ? violation.getPropertyPath().toString() : "unknown";
            String msg = violation.getMessage() != null ? violation.getMessage() : "校验失败";
            fieldErrors.put(field, msg);
        }
        String message = fieldErrors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数约束校验失败");
        String traceId = getTraceId();
        log.warn("参数约束异常: traceId={}, message={}, fieldErrors={}", traceId, message, fieldErrors);
        ValidationException ve = new ValidationException(message, fieldErrors);
        ve.setTraceId(traceId);
        Result result = Result.fail(ve);
        Map<String, Object> data = new HashMap<>();
        data.put("fieldErrors", fieldErrors);
        result.setData(data);
        return buildResult(result);
    }

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String message = "缺少必要参数: " + e.getParameterName();
        String traceId = getTraceId();
        log.warn("缺少参数异常: traceId={}, message={}", traceId, message);
        ValidationException ve = new ValidationException(ErrorCode.PARAM_EMPTY, message);
        ve.setTraceId(traceId);
        return buildResult(Result.fail(ve));
    }

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = "参数类型错误: " + e.getName() + " 应为 " +
                (e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "未知类型");
        String traceId = getTraceId();
        log.warn("参数类型异常: traceId={}, message={}", traceId, message);
        ValidationException ve = new ValidationException(ErrorCode.PARAM_FORMAT_ERROR, message);
        ve.setTraceId(traceId);
        return buildResult(Result.fail(ve));
    }

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        String traceId = getTraceId();
        log.warn("请求体解析异常: traceId={}, message={}", traceId, e.getMessage());
        ValidationException ve = new ValidationException(ErrorCode.PARAM_FORMAT_ERROR, "请求体格式错误或JSON解析失败");
        ve.setTraceId(traceId);
        return buildResult(Result.fail(ve));
    }

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Result handleAuthenticationException(AuthenticationException e) {
        String traceId = getTraceId();
        log.warn("认证异常: traceId={}, message={}", traceId, e.getMessage());
        BusinessException be = new BusinessException(ErrorCode.UNAUTHORIZED);
        be.setTraceId(traceId);
        return buildResult(Result.fail(be));
    }

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result handleAccessDeniedException(AccessDeniedException e) {
        String traceId = getTraceId();
        log.warn("权限异常: traceId={}, message={}", traceId, e.getMessage());
        BusinessException be = new BusinessException(ErrorCode.FORBIDDEN);
        be.setTraceId(traceId);
        return buildResult(Result.fail(be));
    }

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result handleNoHandlerFoundException(NoHandlerFoundException e) {
        String traceId = getTraceId();
        log.warn("接口不存在: traceId={}, url={}", traceId, e.getRequestURL());
        BusinessException be = new BusinessException(ErrorCode.NOT_FOUND, "接口不存在: " + e.getRequestURL());
        be.setTraceId(traceId);
        return buildResult(Result.fail(be));
    }

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        String traceId = getTraceId();
        log.warn("请求方法不支持: traceId={}, method={}", traceId, e.getMethod());
        BusinessException be = new BusinessException(ErrorCode.BAD_REQUEST,
                "请求方法不支持: " + e.getMethod() + "，支持的方法: " +
                        (e.getSupportedHttpMethods() != null ? e.getSupportedHttpMethods() : ""));
        be.setTraceId(traceId);
        return buildResult(Result.fail(be));
    }

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(DuplicateKeyException.class)
    public Result handleDuplicateKeyException(DuplicateKeyException e) {
        String traceId = getTraceId();
        log.error("主键重复异常: traceId={}", traceId, e);
        SystemException se = new SystemException(ErrorCode.DATABASE_ERROR, "数据已存在或唯一约束冲突");
        se.setTraceId(traceId);
        return buildResult(Result.fail(se));
    }

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        String traceId = getTraceId();
        log.error("数据完整性异常: traceId={}", traceId, e);
        SystemException se = new SystemException(ErrorCode.DATABASE_ERROR, "数据操作失败，可能存在外键约束或数据不完整");
        se.setTraceId(traceId);
        return buildResult(Result.fail(se));
    }

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(SQLException.class)
    public Result handleSQLException(SQLException e) {
        String traceId = getTraceId();
        log.error("SQL异常: traceId={}, sqlState={}, errorCode={}", traceId, e.getSQLState(), e.getErrorCode(), e);
        SystemException se = new SystemException(ErrorCode.DATABASE_ERROR, "数据库操作异常");
        se.setTraceId(traceId);
        return buildResult(Result.fail(se));
    }

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(MultipartException.class)
    public Result handleMultipartException(MultipartException e) {
        String traceId = getTraceId();
        log.error("文件上传异常: traceId={}", traceId, e);
        BusinessException be = new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        be.setTraceId(traceId);
        return buildResult(Result.fail(be));
    }

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        String traceId = getTraceId();
        log.warn("文件大小超出限制: traceId={}, message={}", traceId, e.getMessage());
        BusinessException be = new BusinessException(ErrorCode.FILE_SIZE_ERROR);
        be.setTraceId(traceId);
        return buildResult(Result.fail(be));
    }

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(IOException.class)
    public Result handleIOException(IOException e) {
        String traceId = getTraceId();
        log.error("IO异常: traceId={}", traceId, e);
        SystemException se = new SystemException(ErrorCode.FILE_READ_ERROR, "文件操作失败");
        se.setTraceId(traceId);
        return buildResult(Result.fail(se));
    }

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(NullPointerException.class)
    public Result handleNullPointerException(NullPointerException e) {
        String traceId = getTraceId();
        log.error("空指针异常: traceId={}", traceId, e);
        SystemException se = new SystemException(ErrorCode.SYSTEM_ERROR, "系统内部错误（空指针）");
        se.setTraceId(traceId);
        return buildResult(Result.fail(se));
    }

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(IllegalArgumentException.class)
    public Result handleIllegalArgumentException(IllegalArgumentException e) {
        String traceId = getTraceId();
        log.warn("非法参数异常: traceId={}, message={}", traceId, e.getMessage());
        ValidationException ve = new ValidationException(ErrorCode.PARAM_ERROR, e.getMessage());
        ve.setTraceId(traceId);
        return buildResult(Result.fail(ve));
    }

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        String traceId = getTraceId();
        log.error("运行时异常: traceId={}", traceId, e);
        SystemException se = new SystemException(ErrorCode.FAIL, e.getMessage() != null ? e.getMessage() : "运行时错误");
        se.setTraceId(traceId);
        return buildResult(Result.fail(se));
    }

    @SuppressWarnings("rawtypes")
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        String traceId = getTraceId();
        log.error("未知系统异常: traceId={}", traceId, e);
        SystemException se = new SystemException(ErrorCode.SYSTEM_ERROR);
        se.setTraceId(traceId);
        return buildResult(Result.fail(se));
    }
}
