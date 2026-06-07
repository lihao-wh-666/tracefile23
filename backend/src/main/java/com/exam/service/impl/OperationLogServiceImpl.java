package com.exam.service.impl;

import com.exam.entity.OperationLog;
import com.exam.mapper.OperationLogMapper;
import com.exam.service.OperationLogService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public OperationLogServiceImpl(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @Override
    @Async
    public void saveLog(Long userId, String username, String module, String operation,
                        String method, String params, String ip, Integer status, String errorMsg) {
        OperationLog log = new OperationLog();
        log.setUserId(userId);
        log.setUsername(username);
        log.setModule(module);
        log.setOperation(operation);
        log.setMethod(method);
        log.setParams(params);
        log.setIp(ip);
        log.setStatus(status);
        log.setErrorMsg(errorMsg);
        operationLogMapper.insert(log);
    }
}
