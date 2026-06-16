package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.entity.*;
import com.exam.mapper.*;
import com.exam.service.LogArchiveService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.GZIPOutputStream;

@Service
public class LogArchiveServiceImpl implements LogArchiveService {

    private static final Logger log = LoggerFactory.getLogger(LogArchiveServiceImpl.class);

    private static final int TASK_TYPE_HOT_TO_WARM = 1;
    private static final int TASK_TYPE_WARM_TO_COLD = 2;
    private static final int TASK_TYPE_COLD_TO_FILE = 3;

    private static final int STORAGE_LEVEL_HOT = 1;
    private static final int STORAGE_LEVEL_WARM = 2;
    private static final int STORAGE_LEVEL_COLD = 3;
    private static final int STORAGE_LEVEL_FILE = 4;

    private final LogStoragePolicyMapper policyMapper;
    private final LogArchiveTaskMapper taskMapper;
    private final OperationLogMapper operationLogMapper;
    private final OperationLogWarmMapper warmMapper;
    private final OperationLogArchiveMapper archiveMapper;
    private final ObjectMapper objectMapper;

    public LogArchiveServiceImpl(LogStoragePolicyMapper policyMapper,
                                 LogArchiveTaskMapper taskMapper,
                                 OperationLogMapper operationLogMapper,
                                 OperationLogWarmMapper warmMapper,
                                 OperationLogArchiveMapper archiveMapper) {
        this.policyMapper = policyMapper;
        this.taskMapper = taskMapper;
        this.operationLogMapper = operationLogMapper;
        this.warmMapper = warmMapper;
        this.archiveMapper = archiveMapper;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public LogStoragePolicy getStoragePolicy() {
        LogStoragePolicy policy = policyMapper.selectActivePolicy();
        if (policy == null) {
            policy = new LogStoragePolicy();
            policy.setPolicyName("默认合规策略");
            policy.setHotDays(7);
            policy.setWarmDays(90);
            policy.setColdDays(1095);
            policy.setAutoArchiveEnabled(1);
            policy.setArchiveCron("0 0 2 * * ?");
            policy.setFileExportEnabled(0);
            policy.setFileStoragePath("/data/log-archive");
            policy.setFileCompressEnabled(1);
            policy.setIntegrityVerifyEnabled(1);
            policy.setDeleteAfterArchive(1);
            policy.setBatchSize(1000);
        }
        return policy;
    }

    @Override
    public LogStoragePolicy updateStoragePolicy(LogStoragePolicy policy) {
        LogStoragePolicy existing = policyMapper.selectActivePolicy();
        if (existing == null) {
            policyMapper.insert(policy);
        } else {
            policy.setId(existing.getId());
            policyMapper.updateById(policy);
        }
        return getStoragePolicy();
    }

    @Override
    public String createArchiveTask(Integer taskType, LocalDateTime startTime, LocalDateTime endTime,
                                    Long operatorId, String operatorName) {
        String batchId = generateBatchId(taskType);
        LogArchiveTask task = new LogArchiveTask();
        task.setBatchId(batchId);
        task.setTaskType(taskType);
        task.setSourceLevel(getSourceLevel(taskType));
        task.setTargetLevel(getTargetLevel(taskType));
        task.setStartTime(startTime);
        task.setEndTime(endTime);
        task.setTotalCount(0L);
        task.setSuccessCount(0L);
        task.setFailCount(0L);
        task.setStatus(0);
        task.setOperatorId(operatorId);
        task.setOperatorName(operatorName);
        taskMapper.insert(task);
        return batchId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> executeArchiveTask(String batchId) {
        Map<String, Object> result = new HashMap<>();
        LambdaQueryWrapper<LogArchiveTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LogArchiveTask::getBatchId, batchId);
        LogArchiveTask task = taskMapper.selectOne(wrapper);
        if (task == null) {
            result.put("success", false);
            result.put("message", "归档任务不存在：" + batchId);
            return result;
        }
        if (task.getStatus() == 1) {
            result.put("success", false);
            result.put("message", "归档任务正在执行中：" + batchId);
            return result;
        }

        taskMapper.markTaskRunning(task.getId(), LocalDateTime.now());

        try {
            Map<String, Object> execResult;
            switch (task.getTaskType()) {
                case TASK_TYPE_HOT_TO_WARM:
                    execResult = executeHotToWarm(task);
                    break;
                case TASK_TYPE_WARM_TO_COLD:
                    execResult = executeWarmToCold(task);
                    break;
                case TASK_TYPE_COLD_TO_FILE:
                    execResult = executeColdToFile(task);
                    break;
                default:
                    throw new RuntimeException("未知的任务类型：" + task.getTaskType());
            }

            Long successCount = (Long) execResult.get("successCount");
            Long failCount = (Long) execResult.get("failCount");
            Long totalCount = successCount + failCount;
            String filePath = (String) execResult.get("filePath");
            String fileChecksum = (String) execResult.get("fileChecksum");
            Long fileSize = execResult.get("fileSize") != null ? (Long) execResult.get("fileSize") : null;
            boolean success = (Boolean) execResult.get("success");
            String errorMsg = (String) execResult.get("message");

            taskMapper.updateTaskResult(
                    task.getId(),
                    success ? 2 : 3,
                    successCount,
                    failCount,
                    errorMsg,
                    LocalDateTime.now(),
                    filePath,
                    fileChecksum,
                    fileSize
            );

            result.put("success", success);
            result.put("batchId", batchId);
            result.put("totalCount", totalCount);
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("message", errorMsg);
            if (filePath != null) result.put("filePath", filePath);
            if (fileChecksum != null) result.put("fileChecksum", fileChecksum);
            if (fileSize != null) result.put("fileSize", fileSize);
        } catch (Exception e) {
            log.error("执行归档任务异常：{}", batchId, e);
            taskMapper.updateTaskResult(task.getId(), 3, 0L, 0L,
                    e.getMessage(), LocalDateTime.now(), null, null, null);
            result.put("success", false);
            result.put("batchId", batchId);
            result.put("message", "执行异常：" + e.getMessage());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> executeHotToWarm(LogArchiveTask task) {
        Map<String, Object> result = new HashMap<>();
        LogStoragePolicy policy = getStoragePolicy();
        int batchSize = policy.getBatchSize() != null ? policy.getBatchSize() : 1000;
        boolean deleteAfter = policy.getDeleteAfterArchive() != null && policy.getDeleteAfterArchive() == 1;
        boolean verifyIntegrity = policy.getIntegrityVerifyEnabled() != null && policy.getIntegrityVerifyEnabled() == 1;

        LocalDateTime startTime = task.getStartTime();
        LocalDateTime endTime = task.getEndTime();

        long totalCount = operationLogMapper.countByTimeRangeAndStatus(startTime, endTime);
        long successCount = 0;
        long failCount = 0;
        int offset = 0;

        while (offset < totalCount) {
            List<OperationLog> logs = operationLogMapper.selectForMigration(startTime, endTime, offset, batchSize);
            if (logs.isEmpty()) break;

            List<Long> migratedIds = new ArrayList<>();
            for (OperationLog src : logs) {
                try {
                    OperationLogWarm warm = convertToWarm(src, task.getBatchId());
                    warmMapper.insert(warm);
                    migratedIds.add(src.getId());
                    successCount++;
                } catch (Exception e) {
                    log.warn("迁移日志[{}]到温表失败：{}", src.getId(), e.getMessage());
                    failCount++;
                }
            }

            if (!migratedIds.isEmpty()) {
                operationLogMapper.updateArchiveStatusByIds(migratedIds, 1, task.getBatchId());
                if (deleteAfter) {
                    operationLogMapper.deleteByIds(migratedIds);
                }
            }

            offset += logs.size();
        }

        if (verifyIntegrity && successCount > 0) {
            try {
                verifyWarmChainIntegrity(task.getBatchId());
            } catch (Exception e) {
                log.warn("温表完整性校验异常：{}", e.getMessage());
            }
        }

        result.put("success", true);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("message", String.format("热->温迁移完成：成功%d条，失败%d条", successCount, failCount));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> executeWarmToCold(LogArchiveTask task) {
        Map<String, Object> result = new HashMap<>();
        LogStoragePolicy policy = getStoragePolicy();
        int batchSize = policy.getBatchSize() != null ? policy.getBatchSize() : 1000;
        boolean verifyIntegrity = policy.getIntegrityVerifyEnabled() != null && policy.getIntegrityVerifyEnabled() == 1;

        LocalDateTime startTime = task.getStartTime();
        LocalDateTime endTime = task.getEndTime();

        LambdaQueryWrapper<OperationLogWarm> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.ge(OperationLogWarm::getCreateTime, startTime)
                .lt(OperationLogWarm::getCreateTime, endTime)
                .isNull(OperationLogWarm::getArchiveBatchId);
        Long totalCount = warmMapper.selectCount(countWrapper);
        if (totalCount == null) totalCount = 0L;

        long successCount = 0;
        long failCount = 0;
        int offset = 0;

        while (offset < totalCount) {
            LambdaQueryWrapper<OperationLogWarm> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(OperationLogWarm::getCreateTime, startTime)
                    .lt(OperationLogWarm::getCreateTime, endTime)
                    .isNull(OperationLogWarm::getArchiveBatchId)
                    .orderByAsc(OperationLogWarm::getId)
                    .last("LIMIT " + offset + ", " + batchSize);
            List<OperationLogWarm> warms = warmMapper.selectList(wrapper);
            if (warms.isEmpty()) break;

            List<Long> migratedIds = new ArrayList<>();
            for (OperationLogWarm src : warms) {
                try {
                    OperationLogArchive cold = convertToArchive(src, task.getBatchId(), STORAGE_LEVEL_COLD);
                    archiveMapper.insert(cold);
                    migratedIds.add(src.getId());
                    successCount++;
                } catch (Exception e) {
                    log.warn("迁移日志[{}]到冷表失败：{}", src.getId(), e.getMessage());
                    failCount++;
                }
            }

            if (!migratedIds.isEmpty()) {
                for (Long id : migratedIds) {
                    OperationLogWarm update = new OperationLogWarm();
                    update.setId(id);
                    update.setArchiveBatchId(task.getBatchId());
                    warmMapper.updateById(update);
                }
                warmMapper.deleteBatchIds(migratedIds);
            }

            offset += warms.size();
        }

        if (verifyIntegrity && successCount > 0) {
            try {
                verifyArchiveChainIntegrity(task.getBatchId());
            } catch (Exception e) {
                log.warn("冷表完整性校验异常：{}", e.getMessage());
            }
        }

        result.put("success", true);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("message", String.format("温->冷迁移完成：成功%d条，失败%d条", successCount, failCount));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> executeColdToFile(LogArchiveTask task) {
        Map<String, Object> result = new HashMap<>();
        LogStoragePolicy policy = getStoragePolicy();
        int batchSize = policy.getBatchSize() != null ? policy.getBatchSize() : 1000;
        boolean compress = policy.getFileCompressEnabled() != null && policy.getFileCompressEnabled() == 1;
        String storageBasePath = policy.getFileStoragePath();
        if (!StringUtils.hasText(storageBasePath)) {
            storageBasePath = "/data/log-archive";
        }

        LocalDateTime startTime = task.getStartTime();
        LocalDateTime endTime = task.getEndTime();

        Long totalCount = archiveMapper.countByTimeRange(startTime, endTime);
        if (totalCount == null) totalCount = 0L;

        String dateStr = startTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fileName = "log_archive_" + task.getBatchId() + "_" + dateStr + ".json";
        if (compress) fileName += ".gz";

        Path dirPath = Paths.get(storageBasePath,
                startTime.format(DateTimeFormatter.ofPattern("yyyy")),
                startTime.format(DateTimeFormatter.ofPattern("MM")));
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            result.put("success", false);
            result.put("successCount", 0L);
            result.put("failCount", 0L);
            result.put("message", "创建存储目录失败：" + e.getMessage());
            return result;
        }

        Path filePath = dirPath.resolve(fileName);
        long successCount = 0;
        long failCount = 0;
        int offset = 0;
        String fileChecksum = "";

        try (OutputStream fos = Files.newOutputStream(filePath);
             OutputStream os = compress ? new GZIPOutputStream(fos) : fos;
             OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
             BufferedWriter bw = new BufferedWriter(writer)) {

            bw.write("[");
            boolean first = true;

            while (offset < totalCount) {
                List<OperationLogArchive> archives = archiveMapper.selectForFileExport(startTime, endTime, offset, batchSize);
                if (archives.isEmpty()) break;

                List<Long> exportedIds = new ArrayList<>();
                for (OperationLogArchive src : archives) {
                    try {
                        if (!first) bw.write(",");
                        String json = objectMapper.writeValueAsString(src);
                        bw.write(json);
                        first = false;
                        exportedIds.add(src.getId());
                        successCount++;
                    } catch (Exception e) {
                        log.warn("导出日志[{}]到文件失败：{}", src.getId(), e.getMessage());
                        failCount++;
                    }
                }

                if (!exportedIds.isEmpty()) {
                    for (Long id : exportedIds) {
                        OperationLogArchive update = new OperationLogArchive();
                        update.setId(id);
                        update.setStorageLevel(STORAGE_LEVEL_FILE);
                        update.setFilePath(filePath.toString());
                        archiveMapper.updateById(update);
                    }
                }

                offset += archives.size();
            }

            bw.write("]");
            bw.flush();
        } catch (Exception e) {
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException ignored) {
            }
            result.put("success", false);
            result.put("successCount", successCount);
            result.put("failCount", failCount);
            result.put("message", "文件写入失败：" + e.getMessage());
            return result;
        }

        long fileSize;
        try {
            fileSize = Files.size(filePath);
            fileChecksum = computeFileChecksum(filePath);
        } catch (IOException e) {
            fileSize = 0L;
        }

        result.put("success", true);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("filePath", filePath.toString());
        result.put("fileChecksum", fileChecksum);
        result.put("fileSize", fileSize);
        result.put("message", String.format("冷->文件导出完成：成功%d条，失败%d条", successCount, failCount));
        return result;
    }

    @Override
    public IPage<LogArchiveTask> listArchiveTasks(Integer current, Integer size, Integer taskType,
                                                  Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<LogArchiveTask> wrapper = new LambdaQueryWrapper<>();
        if (taskType != null) wrapper.eq(LogArchiveTask::getTaskType, taskType);
        if (status != null) wrapper.eq(LogArchiveTask::getStatus, status);
        if (startTime != null) wrapper.ge(LogArchiveTask::getCreateTime, startTime);
        if (endTime != null) wrapper.le(LogArchiveTask::getCreateTime, endTime);
        wrapper.orderByDesc(LogArchiveTask::getId);
        return taskMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public LogArchiveTask getArchiveTaskDetail(String batchId) {
        LambdaQueryWrapper<LogArchiveTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LogArchiveTask::getBatchId, batchId);
        return taskMapper.selectOne(wrapper);
    }

    @Override
    public Map<String, Object> getStorageStatistics() {
        Map<String, Object> stats = new HashMap<>();
        LogStoragePolicy policy = getStoragePolicy();

        Long hotCount = operationLogMapper.selectCount(new LambdaQueryWrapper<OperationLog>());
        Long warmCount = warmMapper.selectCount(new LambdaQueryWrapper<OperationLogWarm>());
        Long coldCount = archiveMapper.selectCount(new LambdaQueryWrapper<OperationLogArchive>()
                .eq(OperationLogArchive::getStorageLevel, STORAGE_LEVEL_COLD));
        Long fileCount = archiveMapper.selectCount(new LambdaQueryWrapper<OperationLogArchive>()
                .eq(OperationLogArchive::getStorageLevel, STORAGE_LEVEL_FILE));

        stats.put("policy", policy);
        stats.put("hotCount", hotCount != null ? hotCount : 0L);
        stats.put("warmCount", warmCount != null ? warmCount : 0L);
        stats.put("coldCount", coldCount != null ? coldCount : 0L);
        stats.put("fileCount", fileCount != null ? fileCount : 0L);
        stats.put("totalCount", (hotCount != null ? hotCount : 0L)
                + (warmCount != null ? warmCount : 0L)
                + (coldCount != null ? coldCount : 0L)
                + (fileCount != null ? fileCount : 0L));

        List<LogArchiveTask> recentTasks = taskMapper.selectRecentSuccessTasks(5);
        stats.put("recentTasks", recentTasks);

        return stats;
    }

    @Override
    public Map<String, Object> traceLogAcrossLevels(String traceId, Long userId, String targetType,
                                                    String targetId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> result = new HashMap<>();
        List<Object> allLogs = new ArrayList<>();

        List<OperationLog> hotLogs = traceInHot(traceId, userId, targetType, targetId, startTime, endTime);
        List<OperationLogWarm> warmLogs = traceInWarm(traceId, userId, targetType, targetId, startTime, endTime);
        List<OperationLogArchive> coldLogs = traceInCold(traceId, userId, targetType, targetId, startTime, endTime);

        allLogs.addAll(hotLogs);
        allLogs.addAll(warmLogs);
        allLogs.addAll(coldLogs);

        allLogs.sort((a, b) -> {
            LocalDateTime ta = getTime(a);
            LocalDateTime tb = getTime(b);
            return tb.compareTo(ta);
        });

        result.put("total", allLogs.size());
        result.put("hotCount", hotLogs.size());
        result.put("warmCount", warmLogs.size());
        result.put("coldCount", coldLogs.size());
        result.put("logs", allLogs);
        return result;
    }

    @Override
    public Map<String, Object> verifyArchiveIntegrity(String batchId) {
        Map<String, Object> result = new HashMap<>();
        LambdaQueryWrapper<LogArchiveTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LogArchiveTask::getBatchId, batchId);
        LogArchiveTask task = taskMapper.selectOne(wrapper);
        if (task == null) {
            result.put("success", false);
            result.put("message", "归档批次不存在");
            return result;
        }

        try {
            switch (task.getTaskType()) {
                case TASK_TYPE_HOT_TO_WARM:
                    result = verifyWarmChainIntegrity(batchId);
                    break;
                case TASK_TYPE_WARM_TO_COLD:
                    result = verifyArchiveChainIntegrity(batchId);
                    break;
                case TASK_TYPE_COLD_TO_FILE:
                    result = verifyFileIntegrity(task);
                    break;
                default:
                    result.put("success", false);
                    result.put("message", "未知任务类型");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "校验异常：" + e.getMessage());
        }
        return result;
    }

    @Override
    public List<OperationLogArchive> listArchivedLogsForExport(String batchId) {
        LambdaQueryWrapper<OperationLogArchive> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationLogArchive::getArchiveBatchId, batchId);
        wrapper.orderByAsc(OperationLogArchive::getId);
        return archiveMapper.selectList(wrapper);
    }

    private OperationLogWarm convertToWarm(OperationLog src, String batchId) {
        OperationLogWarm warm = new OperationLogWarm();
        warm.setId(src.getId());
        warm.setUserId(src.getUserId());
        warm.setUsername(src.getUsername());
        warm.setModule(src.getModule());
        warm.setOperation(src.getOperation());
        warm.setMethod(src.getMethod());
        warm.setParams(src.getParams());
        warm.setIp(src.getIp());
        warm.setStatus(src.getStatus());
        warm.setErrorMsg(src.getErrorMsg());
        warm.setOperationType(src.getOperationType());
        warm.setTargetType(src.getTargetType());
        warm.setTargetId(src.getTargetId());
        warm.setBeforeState(src.getBeforeState());
        warm.setAfterState(src.getAfterState());
        warm.setUserAgent(src.getUserAgent());
        warm.setTraceId(src.getTraceId());
        warm.setChecksum(src.getChecksum());
        warm.setPreviousChecksum(src.getPreviousChecksum());
        warm.setCreateTime(src.getCreateTime());
        warm.setArchivedTime(LocalDateTime.now());
        warm.setArchiveBatchId(batchId);
        return warm;
    }

    private OperationLogArchive convertToArchive(OperationLogWarm src, String batchId, Integer storageLevel) {
        OperationLogArchive cold = new OperationLogArchive();
        cold.setId(src.getId());
        cold.setUserId(src.getUserId());
        cold.setUsername(src.getUsername());
        cold.setModule(src.getModule());
        cold.setOperation(src.getOperation());
        cold.setMethod(src.getMethod());
        cold.setParams(src.getParams());
        cold.setIp(src.getIp());
        cold.setStatus(src.getStatus());
        cold.setErrorMsg(src.getErrorMsg());
        cold.setOperationType(src.getOperationType());
        cold.setTargetType(src.getTargetType());
        cold.setTargetId(src.getTargetId());
        cold.setBeforeState(src.getBeforeState());
        cold.setAfterState(src.getAfterState());
        cold.setUserAgent(src.getUserAgent());
        cold.setTraceId(src.getTraceId());
        cold.setChecksum(src.getChecksum());
        cold.setPreviousChecksum(src.getPreviousChecksum());
        cold.setCreateTime(src.getCreateTime());
        cold.setArchivedTime(LocalDateTime.now());
        cold.setArchiveBatchId(batchId);
        cold.setStorageLevel(storageLevel);
        return cold;
    }

    private List<OperationLog> traceInHot(String traceId, Long userId, String targetType,
                                          String targetId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(traceId)) wrapper.eq(OperationLog::getTraceId, traceId);
        if (userId != null) wrapper.eq(OperationLog::getUserId, userId);
        if (StringUtils.hasText(targetType)) wrapper.eq(OperationLog::getTargetType, targetType);
        if (StringUtils.hasText(targetId)) wrapper.like(OperationLog::getTargetId, targetId);
        if (startTime != null) wrapper.ge(OperationLog::getCreateTime, startTime);
        if (endTime != null) wrapper.le(OperationLog::getCreateTime, endTime);
        wrapper.orderByDesc(OperationLog::getId);
        wrapper.last("LIMIT 500");
        return operationLogMapper.selectList(wrapper);
    }

    private List<OperationLogWarm> traceInWarm(String traceId, Long userId, String targetType,
                                               String targetId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<OperationLogWarm> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(traceId)) wrapper.eq(OperationLogWarm::getTraceId, traceId);
        if (userId != null) wrapper.eq(OperationLogWarm::getUserId, userId);
        if (StringUtils.hasText(targetType)) wrapper.eq(OperationLogWarm::getTargetType, targetType);
        if (StringUtils.hasText(targetId)) wrapper.like(OperationLogWarm::getTargetId, targetId);
        if (startTime != null) wrapper.ge(OperationLogWarm::getCreateTime, startTime);
        if (endTime != null) wrapper.le(OperationLogWarm::getCreateTime, endTime);
        wrapper.orderByDesc(OperationLogWarm::getId);
        wrapper.last("LIMIT 500");
        return warmMapper.selectList(wrapper);
    }

    private List<OperationLogArchive> traceInCold(String traceId, Long userId, String targetType,
                                                  String targetId, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<OperationLogArchive> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(traceId)) wrapper.eq(OperationLogArchive::getTraceId, traceId);
        if (userId != null) wrapper.eq(OperationLogArchive::getUserId, userId);
        if (StringUtils.hasText(targetType)) wrapper.eq(OperationLogArchive::getTargetType, targetType);
        if (StringUtils.hasText(targetId)) wrapper.like(OperationLogArchive::getTargetId, targetId);
        if (startTime != null) wrapper.ge(OperationLogArchive::getCreateTime, startTime);
        if (endTime != null) wrapper.le(OperationLogArchive::getCreateTime, endTime);
        wrapper.orderByDesc(OperationLogArchive::getId);
        wrapper.last("LIMIT 500");
        return archiveMapper.selectList(wrapper);
    }

    private LocalDateTime getTime(Object obj) {
        if (obj instanceof OperationLog) return ((OperationLog) obj).getCreateTime();
        if (obj instanceof OperationLogWarm) return ((OperationLogWarm) obj).getCreateTime();
        if (obj instanceof OperationLogArchive) return ((OperationLogArchive) obj).getCreateTime();
        return LocalDateTime.now();
    }

    private Map<String, Object> verifyWarmChainIntegrity(String batchId) {
        Map<String, Object> result = new HashMap<>();
        LambdaQueryWrapper<OperationLogWarm> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationLogWarm::getArchiveBatchId, batchId);
        wrapper.orderByAsc(OperationLogWarm::getId);
        List<OperationLogWarm> logs = warmMapper.selectList(wrapper);
        return verifyLogChainIntegrity(logs, result, batchId);
    }

    private Map<String, Object> verifyArchiveChainIntegrity(String batchId) {
        Map<String, Object> result = new HashMap<>();
        LambdaQueryWrapper<OperationLogArchive> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationLogArchive::getArchiveBatchId, batchId);
        wrapper.orderByAsc(OperationLogArchive::getId);
        List<OperationLogArchive> logs = archiveMapper.selectList(wrapper);
        return verifyLogChainIntegrity(logs, result, batchId);
    }

    private Map<String, Object> verifyLogChainIntegrity(List<?> logs, Map<String, Object> result, String batchId) {
        int total = logs.size();
        int valid = 0;
        List<Map<String, Object>> errors = new ArrayList<>();
        String prevChecksum = null;

        for (int i = 0; i < logs.size(); i++) {
            Object obj = logs.get(i);
            Long id = getLogId(obj);
            String currentChecksum = getLogChecksum(obj);
            String storedPrev = getLogPrevChecksum(obj);
            String computedChecksum = computeLogChecksum(obj);

            boolean checksumMatch = currentChecksum != null && currentChecksum.equals(computedChecksum);
            boolean chainMatch = true;
            if (prevChecksum != null && storedPrev != null) {
                chainMatch = prevChecksum.equals(storedPrev);
            }

            if (checksumMatch && chainMatch) {
                valid++;
            } else {
                Map<String, Object> err = new HashMap<>();
                err.put("id", id);
                err.put("checksumMatch", checksumMatch);
                err.put("chainMatch", chainMatch);
                errors.add(err);
            }
            prevChecksum = currentChecksum;
        }

        result.put("success", true);
        result.put("batchId", batchId);
        result.put("total", total);
        result.put("valid", valid);
        result.put("errors", errors);
        result.put("isValid", errors.isEmpty());
        result.put("message", errors.isEmpty() ? "完整性校验通过" : "发现" + errors.size() + "条异常记录");
        return result;
    }

    private Map<String, Object> verifyFileIntegrity(LogArchiveTask task) throws Exception {
        Map<String, Object> result = new HashMap<>();
        String filePath = task.getFilePath();
        if (!StringUtils.hasText(filePath)) {
            result.put("success", false);
            result.put("message", "文件路径为空");
            return result;
        }

        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            result.put("success", false);
            result.put("message", "文件不存在：" + filePath);
            return result;
        }

        String currentChecksum = computeFileChecksum(path);
        boolean match = currentChecksum.equals(task.getFileChecksum());

        result.put("success", true);
        result.put("batchId", task.getBatchId());
        result.put("filePath", filePath);
        result.put("storedChecksum", task.getFileChecksum());
        result.put("currentChecksum", currentChecksum);
        result.put("isValid", match);
        result.put("message", match ? "文件完整性校验通过" : "文件校验和不匹配，可能已被篡改");
        return result;
    }

    private Long getLogId(Object obj) {
        if (obj instanceof OperationLog) return ((OperationLog) obj).getId();
        if (obj instanceof OperationLogWarm) return ((OperationLogWarm) obj).getId();
        if (obj instanceof OperationLogArchive) return ((OperationLogArchive) obj).getId();
        return 0L;
    }

    private String getLogChecksum(Object obj) {
        if (obj instanceof OperationLog) return ((OperationLog) obj).getChecksum();
        if (obj instanceof OperationLogWarm) return ((OperationLogWarm) obj).getChecksum();
        if (obj instanceof OperationLogArchive) return ((OperationLogArchive) obj).getChecksum();
        return null;
    }

    private String getLogPrevChecksum(Object obj) {
        if (obj instanceof OperationLog) return ((OperationLog) obj).getPreviousChecksum();
        if (obj instanceof OperationLogWarm) return ((OperationLogWarm) obj).getPreviousChecksum();
        if (obj instanceof OperationLogArchive) return ((OperationLogArchive) obj).getPreviousChecksum();
        return null;
    }

    private String computeLogChecksum(Object obj) {
        try {
            StringBuilder sb = new StringBuilder();
            if (obj instanceof OperationLog) {
                OperationLog l = (OperationLog) obj;
                sb.append(l.getUserId() == null ? "" : l.getUserId())
                        .append("|").append(l.getUsername() == null ? "" : l.getUsername())
                        .append("|").append(l.getModule() == null ? "" : l.getModule())
                        .append("|").append(l.getOperation() == null ? "" : l.getOperation())
                        .append("|").append(l.getMethod() == null ? "" : l.getMethod())
                        .append("|").append(l.getParams() == null ? "" : l.getParams())
                        .append("|").append(l.getIp() == null ? "" : l.getIp())
                        .append("|").append(l.getStatus() == null ? "" : l.getStatus())
                        .append("|").append(l.getErrorMsg() == null ? "" : l.getErrorMsg())
                        .append("|").append(l.getOperationType() == null ? "" : l.getOperationType())
                        .append("|").append(l.getTargetType() == null ? "" : l.getTargetType())
                        .append("|").append(l.getTargetId() == null ? "" : l.getTargetId())
                        .append("|").append(l.getBeforeState() == null ? "" : l.getBeforeState())
                        .append("|").append(l.getAfterState() == null ? "" : l.getAfterState())
                        .append("|").append(l.getUserAgent() == null ? "" : l.getUserAgent())
                        .append("|").append(l.getTraceId() == null ? "" : l.getTraceId())
                        .append("|").append(l.getPreviousChecksum() == null ? "" : l.getPreviousChecksum());
                if (l.getCreateTime() != null) {
                    sb.append("|").append(l.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
            } else if (obj instanceof OperationLogWarm) {
                OperationLogWarm l = (OperationLogWarm) obj;
                sb.append(l.getUserId() == null ? "" : l.getUserId())
                        .append("|").append(l.getUsername() == null ? "" : l.getUsername())
                        .append("|").append(l.getModule() == null ? "" : l.getModule())
                        .append("|").append(l.getOperation() == null ? "" : l.getOperation())
                        .append("|").append(l.getMethod() == null ? "" : l.getMethod())
                        .append("|").append(l.getParams() == null ? "" : l.getParams())
                        .append("|").append(l.getIp() == null ? "" : l.getIp())
                        .append("|").append(l.getStatus() == null ? "" : l.getStatus())
                        .append("|").append(l.getErrorMsg() == null ? "" : l.getErrorMsg())
                        .append("|").append(l.getOperationType() == null ? "" : l.getOperationType())
                        .append("|").append(l.getTargetType() == null ? "" : l.getTargetType())
                        .append("|").append(l.getTargetId() == null ? "" : l.getTargetId())
                        .append("|").append(l.getBeforeState() == null ? "" : l.getBeforeState())
                        .append("|").append(l.getAfterState() == null ? "" : l.getAfterState())
                        .append("|").append(l.getUserAgent() == null ? "" : l.getUserAgent())
                        .append("|").append(l.getTraceId() == null ? "" : l.getTraceId())
                        .append("|").append(l.getPreviousChecksum() == null ? "" : l.getPreviousChecksum());
                if (l.getCreateTime() != null) {
                    sb.append("|").append(l.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
            } else if (obj instanceof OperationLogArchive) {
                OperationLogArchive l = (OperationLogArchive) obj;
                sb.append(l.getUserId() == null ? "" : l.getUserId())
                        .append("|").append(l.getUsername() == null ? "" : l.getUsername())
                        .append("|").append(l.getModule() == null ? "" : l.getModule())
                        .append("|").append(l.getOperation() == null ? "" : l.getOperation())
                        .append("|").append(l.getMethod() == null ? "" : l.getMethod())
                        .append("|").append(l.getParams() == null ? "" : l.getParams())
                        .append("|").append(l.getIp() == null ? "" : l.getIp())
                        .append("|").append(l.getStatus() == null ? "" : l.getStatus())
                        .append("|").append(l.getErrorMsg() == null ? "" : l.getErrorMsg())
                        .append("|").append(l.getOperationType() == null ? "" : l.getOperationType())
                        .append("|").append(l.getTargetType() == null ? "" : l.getTargetType())
                        .append("|").append(l.getTargetId() == null ? "" : l.getTargetId())
                        .append("|").append(l.getBeforeState() == null ? "" : l.getBeforeState())
                        .append("|").append(l.getAfterState() == null ? "" : l.getAfterState())
                        .append("|").append(l.getUserAgent() == null ? "" : l.getUserAgent())
                        .append("|").append(l.getTraceId() == null ? "" : l.getTraceId())
                        .append("|").append(l.getPreviousChecksum() == null ? "" : l.getPreviousChecksum());
                if (l.getCreateTime() != null) {
                    sb.append("|").append(l.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
            }
            return sha256(sb.toString());
        } catch (Exception e) {
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    private String computeFileChecksum(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                digest.update(buffer, 0, len);
            }
            byte[] hash = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new IOException("计算文件校验和失败", e);
        }
    }

    private String sha256(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String generateBatchId(int taskType) {
        String prefix = "";
        switch (taskType) {
            case TASK_TYPE_HOT_TO_WARM:
                prefix = "HTW";
                break;
            case TASK_TYPE_WARM_TO_COLD:
                prefix = "WTC";
                break;
            case TASK_TYPE_COLD_TO_FILE:
                prefix = "CTF";
                break;
            default:
                prefix = "LOG";
        }
        return prefix + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private int getSourceLevel(int taskType) {
        switch (taskType) {
            case TASK_TYPE_HOT_TO_WARM:
                return STORAGE_LEVEL_HOT;
            case TASK_TYPE_WARM_TO_COLD:
                return STORAGE_LEVEL_WARM;
            case TASK_TYPE_COLD_TO_FILE:
                return STORAGE_LEVEL_COLD;
            default:
                return 0;
        }
    }

    private int getTargetLevel(int taskType) {
        switch (taskType) {
            case TASK_TYPE_HOT_TO_WARM:
                return STORAGE_LEVEL_WARM;
            case TASK_TYPE_WARM_TO_COLD:
                return STORAGE_LEVEL_COLD;
            case TASK_TYPE_COLD_TO_FILE:
                return STORAGE_LEVEL_FILE;
            default:
                return 0;
        }
    }
}
