package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.entity.OperationLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OperationLogService {
    void saveLog(Long userId, String username, String module, String operation,
                 String method, String params, String ip, Integer status, String errorMsg);

    void saveLogWithIntegrity(OperationLog log);

    String getTargetState(String targetType, String targetId);

    IPage<OperationLog> page(Integer current, Integer size, String keyword, Integer operationType,
                             String module, String username, String targetType, String targetId,
                             Integer status, LocalDateTime startTime, LocalDateTime endTime);

    OperationLog getDetail(Long id);

    Map<String, Object> verifyIntegrity(Long startId, Long endId);

    Map<String, Object> getStatistics(LocalDateTime startTime, LocalDateTime endTime);

    List<OperationLog> listForExport(String keyword, Integer operationType,
                                     String module, String username, String targetType,
                                     Integer status, LocalDateTime startTime, LocalDateTime endTime);
}
