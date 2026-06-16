package com.exam.aspect;

import com.exam.annotation.Log;
import com.exam.common.TraceIdContext;
import com.exam.entity.OperationLog;
import com.exam.entity.User;
import com.exam.mapper.UserMapper;
import com.exam.service.OperationLogService;
import com.exam.util.IpUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

@Aspect
@Component
public class LogAspect {

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;

    public LogAspect(OperationLogService operationLogService, ObjectMapper objectMapper, UserMapper userMapper) {
        this.operationLogService = operationLogService;
        this.objectMapper = objectMapper;
        this.userMapper = userMapper;
    }

    @Around("@annotation(com.exam.annotation.Log)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Log logAnnotation = method.getAnnotation(Log.class);

        String module = logAnnotation.module();
        String operation = logAnnotation.operation();
        Integer operationType = logAnnotation.operationType();
        String targetType = logAnnotation.targetType();
        boolean recordState = logAnnotation.recordState();
        String methodName = point.getTarget().getClass().getName() + "." + method.getName();

        String params = "";
        String targetId = "";
        try {
            Object[] args = point.getArgs();
            if (args != null && args.length > 0) {
                List<Object> filteredArgs = new ArrayList<>();
                for (Object arg : args) {
                    if (arg instanceof MultipartFile) {
                        MultipartFile file = (MultipartFile) arg;
                        filteredArgs.add("[文件] " + file.getOriginalFilename() + " (" + file.getSize() + " bytes)");
                    } else if (arg instanceof MultipartFile[]) {
                        MultipartFile[] files = (MultipartFile[]) arg;
                        filteredArgs.add("[文件数组] 共 " + files.length + " 个文件");
                    } else if (arg instanceof HttpServletRequest) {
                        filteredArgs.add("[HttpServletRequest]");
                    } else if (arg instanceof HttpServletResponse) {
                        filteredArgs.add("[HttpServletResponse]");
                    } else if (arg != null && arg.getClass().getName().startsWith("javax.servlet")
                            || (arg != null && arg.getClass().getName().startsWith("org.springframework.web"))) {
                        filteredArgs.add("[" + arg.getClass().getSimpleName() + "]");
                    } else {
                        filteredArgs.add(arg);
                    }
                    if (arg instanceof Long || arg instanceof String) {
                        if (targetId.isEmpty() && (method.getName().contains("getById")
                                || method.getName().contains("update")
                                || method.getName().contains("remove")
                                || method.getName().contains("delete")
                                || method.getName().contains("unlock")
                                || method.getName().contains("status"))) {
                            if (method.getParameters().length > 0) {
                                String firstParamName = method.getParameters()[0].getName();
                                if ("id".equals(firstParamName)) {
                                    targetId = arg.toString();
                                }
                            }
                        }
                    }
                }
                params = objectMapper.writeValueAsString(filteredArgs);
                if (params.length() > 2000) {
                    params = params.substring(0, 2000) + "...";
                }
            }
        } catch (Throwable e) {
            params = "参数解析失败: " + e.getMessage();
        }

        String ip = "";
        String username = "";
        Long userId = null;
        String userAgent = "";
        String traceId = TraceIdContext.getTraceId();
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ip = IpUtil.getIpAddr(request);
                userAgent = request.getHeader("User-Agent");
                if (userAgent != null && userAgent.length() > 500) {
                    userAgent = userAgent.substring(0, 500);
                }
            }
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal != null && !"anonymousUser".equals(principal.toString())) {
                userId = Long.parseLong(principal.toString());
                User user = userMapper.selectById(userId);
                if (user != null) {
                    username = user.getUsername();
                }
            }
        } catch (Exception e) {
            // ignore
        }

        Integer status = 1;
        String errorMsg = "";
        Object result = null;
        String beforeState = "";
        String afterState = "";

        if (recordState && !targetId.isEmpty() && !targetType.isEmpty()) {
            try {
                beforeState = operationLogService.getTargetState(targetType, targetId);
            } catch (Exception e) {
                beforeState = "";
            }
        }

        try {
            result = point.proceed();
        } catch (Throwable e) {
            status = 0;
            errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.length() > 500) {
                errorMsg = errorMsg.substring(0, 500);
            }
            throw e;
        } finally {
            if (recordState && status == 1 && !targetId.isEmpty() && !targetType.isEmpty()) {
                try {
                    afterState = operationLogService.getTargetState(targetType, targetId);
                } catch (Throwable e) {
                    afterState = "";
                }
            }

            try {
                OperationLog log = new OperationLog();
                log.setUserId(userId);
                log.setUsername(username);
                log.setModule(module);
                log.setOperation(operation);
                log.setMethod(methodName);
                log.setParams(params);
                log.setIp(ip);
                log.setStatus(status);
                log.setErrorMsg(errorMsg);
                log.setOperationType(operationType);
                log.setTargetType(targetType);
                log.setTargetId(targetId.isEmpty() ? null : targetId);
                log.setBeforeState(beforeState.isEmpty() ? null : beforeState);
                log.setAfterState(afterState.isEmpty() ? null : afterState);
                log.setUserAgent(userAgent.isEmpty() ? null : userAgent);
                log.setTraceId(traceId);

                operationLogService.saveLogWithIntegrity(log);
            } catch (Throwable t) {
                log.error("保存操作日志失败", t);
            }
        }

        return result;
    }
}
