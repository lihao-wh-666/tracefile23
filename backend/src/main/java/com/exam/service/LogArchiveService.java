package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.entity.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface LogArchiveService {

    LogStoragePolicy getStoragePolicy();

    LogStoragePolicy updateStoragePolicy(LogStoragePolicy policy);

    String createArchiveTask(Integer taskType, LocalDateTime startTime, LocalDateTime endTime,
                             Long operatorId, String operatorName);

    Map<String, Object> executeArchiveTask(String batchId);

    Map<String, Object> executeHotToWarm(LogArchiveTask task);

    Map<String, Object> executeWarmToCold(LogArchiveTask task);

    Map<String, Object> executeColdToFile(LogArchiveTask task);

    IPage<LogArchiveTask> listArchiveTasks(Integer current, Integer size, Integer taskType,
                                           Integer status, LocalDateTime startTime, LocalDateTime endTime);

    LogArchiveTask getArchiveTaskDetail(String batchId);

    Map<String, Object> getStorageStatistics();

    Map<String, Object> traceLogAcrossLevels(String traceId, Long userId, String targetType,
                                             String targetId, LocalDateTime startTime, LocalDateTime endTime);

    Map<String, Object> verifyArchiveIntegrity(String batchId);

    List<OperationLogArchive> listArchivedLogsForExport(String batchId);
}
