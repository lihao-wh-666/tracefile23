package com.exam.aspect;

import com.exam.annotation.Log;
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
import java.lang.reflect.Method;

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
        String methodName = point.getTarget().getClass().getName() + "." + method.getName();

        String params = "";
        try {
            Object[] args = point.getArgs();
            if (args != null && args.length > 0) {
                params = objectMapper.writeValueAsString(args);
                if (params.length() > 2000) {
                    params = params.substring(0, 2000) + "...";
                }
            }
        } catch (Exception e) {
            params = "参数解析失败";
        }

        String ip = "";
        String username = "";
        Long userId = null;
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ip = IpUtil.getIpAddr(request);
            }
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal != null) {
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
            operationLogService.saveLog(userId, username, module, operation,
                    methodName, params, ip, status, errorMsg);
        }

        return result;
    }
}
