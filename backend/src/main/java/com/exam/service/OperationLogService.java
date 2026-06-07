package com.exam.service;

public interface OperationLogService {
    void saveLog(Long userId, String username, String module, String operation,
                 String method, String params, String ip, Integer status, String errorMsg);
}
