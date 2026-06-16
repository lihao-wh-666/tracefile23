package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.entity.OperationLog;
import com.exam.entity.OperationLogArchive;
import com.exam.entity.OperationLogWarm;
import com.exam.mapper.*;
import com.exam.service.OperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    private final OperationLogMapper operationLogMapper;
    private final OperationLogWarmMapper warmMapper;
    private final OperationLogArchiveMapper archiveMapper;
    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;
    private final PaperMapper paperMapper;
    private final ExamMapper examMapper;
    private final SubjectMapper subjectMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final ObjectMapper objectMapper;

    public OperationLogServiceImpl(OperationLogMapper operationLogMapper,
                                   OperationLogWarmMapper warmMapper,
                                   OperationLogArchiveMapper archiveMapper,
                                   UserMapper userMapper,
                                   QuestionMapper questionMapper,
                                   PaperMapper paperMapper,
                                   ExamMapper examMapper,
                                   SubjectMapper subjectMapper,
                                   SystemConfigMapper systemConfigMapper,
                                   ObjectMapper objectMapper) {
        this.operationLogMapper = operationLogMapper;
        this.warmMapper = warmMapper;
        this.archiveMapper = archiveMapper;
        this.userMapper = userMapper;
        this.questionMapper = questionMapper;
        this.paperMapper = paperMapper;
        this.examMapper = examMapper;
        this.subjectMapper = subjectMapper;
        this.systemConfigMapper = systemConfigMapper;
        this.objectMapper = objectMapper;
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
        saveLogWithIntegrity(log);
    }

    @Override
    public synchronized void saveLogWithIntegrity(OperationLog log) {
        try {
            String lastChecksum = null;
            try {
                lastChecksum = operationLogMapper.selectLastChecksum();
            } catch (Exception ignored) {
            }
            log.setPreviousChecksum(lastChecksum);
            String checksum = computeChecksum(log);
            log.setChecksum(checksum);
            operationLogMapper.insert(log);
        } catch (Exception e) {
            OperationLog fallback = new OperationLog();
            fallback.setUserId(log.getUserId());
            fallback.setUsername(log.getUsername());
            fallback.setModule(log.getModule());
            fallback.setOperation(log.getOperation());
            fallback.setMethod(log.getMethod());
            fallback.setParams(log.getParams());
            fallback.setIp(log.getIp());
            fallback.setStatus(log.getStatus());
            fallback.setErrorMsg(log.getErrorMsg());
            try {
                operationLogMapper.insert(fallback);
            } catch (Exception ignored) {
            }
        }
    }

    private String computeChecksum(OperationLog log) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(log.getUserId() == null ? "" : log.getUserId());
            sb.append("|").append(log.getUsername() == null ? "" : log.getUsername());
            sb.append("|").append(log.getModule() == null ? "" : log.getModule());
            sb.append("|").append(log.getOperation() == null ? "" : log.getOperation());
            sb.append("|").append(log.getMethod() == null ? "" : log.getMethod());
            sb.append("|").append(log.getParams() == null ? "" : log.getParams());
            sb.append("|").append(log.getIp() == null ? "" : log.getIp());
            sb.append("|").append(log.getStatus() == null ? "" : log.getStatus());
            sb.append("|").append(log.getErrorMsg() == null ? "" : log.getErrorMsg());
            sb.append("|").append(log.getOperationType() == null ? "" : log.getOperationType());
            sb.append("|").append(log.getTargetType() == null ? "" : log.getTargetType());
            sb.append("|").append(log.getTargetId() == null ? "" : log.getTargetId());
            sb.append("|").append(log.getBeforeState() == null ? "" : log.getBeforeState());
            sb.append("|").append(log.getAfterState() == null ? "" : log.getAfterState());
            sb.append("|").append(log.getUserAgent() == null ? "" : log.getUserAgent());
            sb.append("|").append(log.getTraceId() == null ? "" : log.getTraceId());
            sb.append("|").append(log.getPreviousChecksum() == null ? "" : log.getPreviousChecksum());
            if (log.getCreateTime() != null) {
                sb.append("|").append(log.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString().replace("-", "");
        }
    }

    @Override
    public String getTargetState(String targetType, String targetId) {
        try {
            Object entity = null;
            Long id = Long.parseLong(targetId);
            switch (targetType.toLowerCase()) {
                case "user":
                    entity = userMapper.selectById(id);
                    break;
                case "question":
                    entity = questionMapper.selectById(id);
                    break;
                case "paper":
                    entity = paperMapper.selectById(id);
                    break;
                case "exam":
                    entity = examMapper.selectById(id);
                    break;
                case "subject":
                    entity = subjectMapper.selectById(id);
                    break;
                case "systemconfig":
                case "system_config":
                    entity = systemConfigMapper.selectById(id);
                    break;
                default:
                    return "";
            }
            if (entity != null) {
                String json = objectMapper.writeValueAsString(entity);
                if (json.length() > 4000) {
                    json = json.substring(0, 4000) + "...";
                }
                return json;
            }
        } catch (Exception e) {
            // ignore
        }
        return "";
    }

    @Override
    public IPage<OperationLog> page(Integer current, Integer size, String keyword, Integer operationType,
                                    String module, String username, String targetType, String targetId,
                                    Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(OperationLog::getUsername, keyword)
                        .or().like(OperationLog::getModule, keyword)
                        .or().like(OperationLog::getOperation, keyword)
                        .or().like(OperationLog::getTargetType, keyword)
                        .or().like(OperationLog::getTargetId, keyword)
                        .or().like(OperationLog::getIp, keyword)
                        .or().like(OperationLog::getParams, keyword)
                        .or().like(OperationLog::getTraceId, keyword));
            }
            if (operationType != null) {
                wrapper.eq(OperationLog::getOperationType, operationType);
            }
            if (StringUtils.hasText(module)) {
                wrapper.like(OperationLog::getModule, module);
            }
            if (StringUtils.hasText(username)) {
                wrapper.like(OperationLog::getUsername, username);
            }
            if (StringUtils.hasText(targetType)) {
                wrapper.eq(OperationLog::getTargetType, targetType);
            }
            if (StringUtils.hasText(targetId)) {
                wrapper.like(OperationLog::getTargetId, targetId);
            }
            if (status != null) {
                wrapper.eq(OperationLog::getStatus, status);
            }
            if (startTime != null) {
                wrapper.ge(OperationLog::getCreateTime, startTime);
            }
            if (endTime != null) {
                wrapper.le(OperationLog::getCreateTime, endTime);
            }
            wrapper.orderByDesc(OperationLog::getId);
            return operationLogMapper.selectPage(new Page<>(current, size), wrapper);
        } catch (Exception e) {
            LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(OperationLog::getUsername, keyword)
                        .or().like(OperationLog::getModule, keyword)
                        .or().like(OperationLog::getOperation, keyword)
                        .or().like(OperationLog::getIp, keyword)
                        .or().like(OperationLog::getParams, keyword));
            }
            if (StringUtils.hasText(module)) {
                wrapper.like(OperationLog::getModule, module);
            }
            if (StringUtils.hasText(username)) {
                wrapper.like(OperationLog::getUsername, username);
            }
            if (status != null) {
                wrapper.eq(OperationLog::getStatus, status);
            }
            if (startTime != null) {
                wrapper.ge(OperationLog::getCreateTime, startTime);
            }
            if (endTime != null) {
                wrapper.le(OperationLog::getCreateTime, endTime);
            }
            wrapper.orderByDesc(OperationLog::getId);
            return operationLogMapper.selectPage(new Page<>(current, size), wrapper);
        }
    }

    @Override
    public OperationLog getDetail(Long id) {
        try {
            OperationLog log = operationLogMapper.selectById(id);
            if (log != null) {
                OperationLog ext = operationLogMapper.selectDetailWithExt(id);
                if (ext != null) {
                    log.setOperationType(ext.getOperationType());
                    log.setTargetType(ext.getTargetType());
                    log.setTargetId(ext.getTargetId());
                    log.setBeforeState(ext.getBeforeState());
                    log.setAfterState(ext.getAfterState());
                    log.setUserAgent(ext.getUserAgent());
                    log.setTraceId(ext.getTraceId());
                    log.setChecksum(ext.getChecksum());
                    log.setPreviousChecksum(ext.getPreviousChecksum());
                }
            }
            return log;
        } catch (Exception e) {
            return operationLogMapper.selectById(id);
        }
    }

    @Override
    public Map<String, Object> verifyIntegrity(Long startId, Long endId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> chain = operationLogMapper.selectChecksumChain(startId, endId);
            List<Map<String, Object>> errors = new ArrayList<>();
            int total = chain.size();
            int validCount = 0;

            String prevChecksum = null;
            for (int i = 0; i < chain.size(); i++) {
                Map<String, Object> row = chain.get(i);
                Long id = ((Number) row.get("id")).longValue();
                String currentChecksum = (String) row.get("checksum");
                String storedPrevChecksum = (String) row.get("previous_checksum");

                boolean prevMatch = true;
                if (i > 0 && prevChecksum != null) {
                    prevMatch = prevChecksum.equals(storedPrevChecksum);
                }

                OperationLog log = operationLogMapper.selectById(id);
                if (log != null) {
                    log.setPreviousChecksum(storedPrevChecksum);
                    OperationLog ext = operationLogMapper.selectDetailWithExt(id);
                    if (ext != null) {
                        log.setOperationType(ext.getOperationType());
                        log.setTargetType(ext.getTargetType());
                        log.setTargetId(ext.getTargetId());
                        log.setBeforeState(ext.getBeforeState());
                        log.setAfterState(ext.getAfterState());
                        log.setUserAgent(ext.getUserAgent());
                        log.setTraceId(ext.getTraceId());
                        log.setChecksum(ext.getChecksum());
                    }
                    String computedChecksum = computeChecksum(log);
                    boolean checksumMatch = computedChecksum.equals(currentChecksum);

                    if (checksumMatch && prevMatch) {
                        validCount++;
                    } else {
                        Map<String, Object> error = new HashMap<>();
                        error.put("id", id);
                        error.put("checksumMatch", checksumMatch);
                        error.put("prevChainMatch", prevMatch);
                        errors.add(error);
                    }
                }
                prevChecksum = currentChecksum;
            }

            result.put("total", total);
            result.put("valid", validCount);
            result.put("errors", errors);
            result.put("isValid", errors.isEmpty() && total > 0);
            result.put("featureEnabled", true);
        } catch (Exception e) {
            result.put("total", 0);
            result.put("valid", 0);
            result.put("errors", Collections.emptyList());
            result.put("isValid", false);
            result.put("featureEnabled", false);
            result.put("message", "请先执行 add_operation_log_enhancement.sql 启用完整性校验功能");
        }
        return result;
    }

    @Override
    public Map<String, Object> getStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> result = new HashMap<>();
        String startStr = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endStr = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        List<Map<String, Object>> operationTypeStats = Collections.emptyList();
        List<Map<String, Object>> moduleStats = Collections.emptyList();
        List<Map<String, Object>> userStats = Collections.emptyList();
        List<Map<String, Object>> dateStats = Collections.emptyList();
        List<Map<String, Object>> statusStats = Collections.emptyList();
        boolean featureEnabled = true;
        long totalCount = 0;
        long successCount = 0;
        long failCount = 0;

        try {
            operationTypeStats = operationLogMapper.selectOperationTypeStats(startStr, endStr);
            moduleStats = operationLogMapper.selectModuleStats(startStr, endStr);
            userStats = operationLogMapper.selectUserStats(startStr, endStr);
            dateStats = operationLogMapper.selectDateStats(startStr, endStr);
            statusStats = operationLogMapper.selectStatusStats(startStr, endStr);
            for (Map<String, Object> stat : statusStats) {
                long cnt = ((Number) stat.get("cnt")).longValue();
                Integer status = ((Number) stat.get("status")).intValue();
                totalCount += cnt;
                if (status == 1) successCount += cnt;
                else failCount += cnt;
            }
        } catch (Exception e) {
            featureEnabled = false;
            try {
                LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
                wrapper.ge(OperationLog::getCreateTime, startTime);
                wrapper.le(OperationLog::getCreateTime, endTime);
                wrapper.select(OperationLog::getStatus);
                List<OperationLog> all = operationLogMapper.selectList(wrapper);
                totalCount = all.size();
                for (OperationLog log : all) {
                    if (log.getStatus() != null && log.getStatus() == 1) successCount++;
                    else failCount++;
                }
                moduleStats = buildFallbackModuleStats(startTime, endTime);
                userStats = buildFallbackUserStats(startTime, endTime);
                dateStats = buildFallbackDateStats(startTime, endTime);
            } catch (Exception ignored) {
            }
        }

        result.put("operationTypeStats", operationTypeStats);
        result.put("moduleStats", moduleStats);
        result.put("userStats", userStats);
        result.put("dateStats", dateStats);
        result.put("statusStats", statusStats);
        result.put("totalCount", totalCount);
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("featureEnabled", featureEnabled);
        return result;
    }

    private List<Map<String, Object>> buildFallbackModuleStats(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(OperationLog::getCreateTime, start);
        wrapper.le(OperationLog::getCreateTime, end);
        wrapper.select(OperationLog::getModule);
        List<OperationLog> logs = operationLogMapper.selectList(wrapper);
        Map<String, Long> map = new LinkedHashMap<>();
        for (OperationLog log : logs) {
            String m = log.getModule() == null ? "(未分类)" : log.getModule();
            map.merge(m, 1L, Long::sum);
        }
        return mapToList(map, 10, "module");
    }

    private List<Map<String, Object>> buildFallbackUserStats(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(OperationLog::getCreateTime, start);
        wrapper.le(OperationLog::getCreateTime, end);
        wrapper.select(OperationLog::getUsername);
        List<OperationLog> logs = operationLogMapper.selectList(wrapper);
        Map<String, Long> map = new LinkedHashMap<>();
        for (OperationLog log : logs) {
            String u = log.getUsername() == null ? "(匿名)" : log.getUsername();
            map.merge(u, 1L, Long::sum);
        }
        return mapToList(map, 10, "username");
    }

    private List<Map<String, Object>> buildFallbackDateStats(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.ge(OperationLog::getCreateTime, start);
        wrapper.le(OperationLog::getCreateTime, end);
        wrapper.select(OperationLog::getCreateTime);
        List<OperationLog> logs = operationLogMapper.selectList(wrapper);
        Map<String, Long> map = new TreeMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (OperationLog log : logs) {
            if (log.getCreateTime() != null) {
                String d = log.getCreateTime().format(fmt);
                map.merge(d, 1L, Long::sum);
            }
        }
        return mapToList(map, null, "date");
    }

    private List<Map<String, Object>> mapToList(Map<String, Long> map, Integer limit, String keyName) {
        List<Map.Entry<String, Long>> list = new ArrayList<>(map.entrySet());
        list.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        if (limit != null && list.size() > limit) {
            list = list.subList(0, limit);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Long> e : list) {
            Map<String, Object> m = new HashMap<>();
            m.put(keyName, e.getKey());
            m.put("cnt", e.getValue());
            result.add(m);
        }
        return result;
    }

    @Override
    public List<OperationLog> listForExport(String keyword, Integer operationType,
                                            String module, String username, String targetType,
                                            Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(OperationLog::getUsername, keyword)
                        .or().like(OperationLog::getModule, keyword)
                        .or().like(OperationLog::getOperation, keyword)
                        .or().like(OperationLog::getTargetType, keyword)
                        .or().like(OperationLog::getIp, keyword));
            }
            if (operationType != null) wrapper.eq(OperationLog::getOperationType, operationType);
            if (StringUtils.hasText(module)) wrapper.like(OperationLog::getModule, module);
            if (StringUtils.hasText(username)) wrapper.like(OperationLog::getUsername, username);
            if (StringUtils.hasText(targetType)) wrapper.eq(OperationLog::getTargetType, targetType);
            if (status != null) wrapper.eq(OperationLog::getStatus, status);
            if (startTime != null) wrapper.ge(OperationLog::getCreateTime, startTime);
            if (endTime != null) wrapper.le(OperationLog::getCreateTime, endTime);
            wrapper.orderByDesc(OperationLog::getId);
            wrapper.last("LIMIT 10000");
            return operationLogMapper.selectList(wrapper);
        } catch (Exception e) {
            LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(OperationLog::getUsername, keyword)
                        .or().like(OperationLog::getModule, keyword)
                        .or().like(OperationLog::getOperation, keyword)
                        .or().like(OperationLog::getIp, keyword));
            }
            if (StringUtils.hasText(module)) wrapper.like(OperationLog::getModule, module);
            if (StringUtils.hasText(username)) wrapper.like(OperationLog::getUsername, username);
            if (status != null) wrapper.eq(OperationLog::getStatus, status);
            if (startTime != null) wrapper.ge(OperationLog::getCreateTime, startTime);
            if (endTime != null) wrapper.le(OperationLog::getCreateTime, endTime);
            wrapper.orderByDesc(OperationLog::getId);
            wrapper.last("LIMIT 10000");
            return operationLogMapper.selectList(wrapper);
        }
    }

    @Override
    public OperationLog getDetail(Long id) {
        OperationLog log = null;
        try {
            log = operationLogMapper.selectById(id);
            if (log != null) {
                OperationLog ext = operationLogMapper.selectDetailWithExt(id);
                if (ext != null) {
                    log.setOperationType(ext.getOperationType());
                    log.setTargetType(ext.getTargetType());
                    log.setTargetId(ext.getTargetId());
                    log.setBeforeState(ext.getBeforeState());
                    log.setAfterState(ext.getAfterState());
                    log.setUserAgent(ext.getUserAgent());
                    log.setTraceId(ext.getTraceId());
                    log.setChecksum(ext.getChecksum());
                    log.setPreviousChecksum(ext.getPreviousChecksum());
                    log.setArchiveStatus(ext.getArchiveStatus());
                    log.setArchiveBatchId(ext.getArchiveBatchId());
                }
                log.setArchiveStatus(log.getArchiveStatus() == null ? 0 : log.getArchiveStatus());
                return log;
            }
        } catch (Exception ignored) {
        }

        try {
            OperationLogWarm warm = warmMapper.selectById(id);
            if (warm != null) {
                return convertWarmToLog(warm);
            }
        } catch (Exception ignored) {
        }

        try {
            OperationLogArchive cold = archiveMapper.selectById(id);
            if (cold != null) {
                return convertArchiveToLog(cold);
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    @Override
    public IPage<Map<String, Object>> pageCrossTier(Integer current, Integer size, String keyword, Integer operationType,
                                                     String module, String username, String targetType, String targetId,
                                                     Integer status, LocalDateTime startTime, LocalDateTime endTime,
                                                     Boolean includeArchived) {
        List<Map<String, Object>> allRecords = new ArrayList<>();

        List<OperationLog> hotLogs = queryHot(keyword, operationType, module, username,
                targetType, targetId, status, startTime, endTime);
        for (OperationLog log : hotLogs) {
            allRecords.add(convertLogToMap(log, "HOT"));
        }

        if (Boolean.TRUE.equals(includeArchived)) {
            List<OperationLogWarm> warmLogs = queryWarm(keyword, operationType, module, username,
                    targetType, targetId, status, startTime, endTime);
            for (OperationLogWarm w : warmLogs) {
                allRecords.add(convertWarmToMap(w));
            }

            List<OperationLogArchive> coldLogs = queryCold(keyword, operationType, module, username,
                    targetType, targetId, status, startTime, endTime);
            for (OperationLogArchive c : coldLogs) {
                allRecords.add(convertArchiveToMap(c));
            }
        }

        allRecords.sort((a, b) -> {
            LocalDateTime ta = (LocalDateTime) a.get("createTime");
            LocalDateTime tb = (LocalDateTime) b.get("createTime");
            if (ta == null && tb == null) return 0;
            if (ta == null) return 1;
            if (tb == null) return -1;
            return tb.compareTo(ta);
        });

        int total = allRecords.size();
        int from = Math.min((current - 1) * size, total);
        int to = Math.min(from + size, total);
        List<Map<String, Object>> pageRecords = allRecords.subList(from, to);

        Page<Map<String, Object>> page = new Page<>(current, size, total);
        page.setRecords(pageRecords);
        return page;
    }

    private List<OperationLog> queryHot(String keyword, Integer operationType, String module, String username,
                                        String targetType, String targetId, Integer status,
                                        LocalDateTime startTime, LocalDateTime endTime) {
        try {
            LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
            applyQueryCondition(wrapper, keyword, operationType, module, username,
                    targetType, targetId, status, startTime, endTime, OperationLog.class);
            wrapper.orderByDesc(OperationLog::getId);
            wrapper.last("LIMIT 1000");
            return operationLogMapper.selectList(wrapper);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<OperationLogWarm> queryWarm(String keyword, Integer operationType, String module, String username,
                                             String targetType, String targetId, Integer status,
                                             LocalDateTime startTime, LocalDateTime endTime) {
        try {
            LambdaQueryWrapper<OperationLogWarm> wrapper = new LambdaQueryWrapper<>();
            applyQueryConditionWarm(wrapper, keyword, operationType, module, username,
                    targetType, targetId, status, startTime, endTime);
            wrapper.orderByDesc(OperationLogWarm::getId);
            wrapper.last("LIMIT 1000");
            return warmMapper.selectList(wrapper);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<OperationLogArchive> queryCold(String keyword, Integer operationType, String module, String username,
                                                String targetType, String targetId, Integer status,
                                                LocalDateTime startTime, LocalDateTime endTime) {
        try {
            LambdaQueryWrapper<OperationLogArchive> wrapper = new LambdaQueryWrapper<>();
            applyQueryConditionArchive(wrapper, keyword, operationType, module, username,
                    targetType, targetId, status, startTime, endTime);
            wrapper.orderByDesc(OperationLogArchive::getId);
            wrapper.last("LIMIT 1000");
            return archiveMapper.selectList(wrapper);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void applyQueryCondition(LambdaQueryWrapper<T> wrapper, String keyword, Integer operationType,
                                         String module, String username, String targetType, String targetId,
                                         Integer status, LocalDateTime startTime, LocalDateTime endTime, Class<T> clazz) {
        if (StringUtils.hasText(keyword)) {
            if (clazz == OperationLog.class) {
                LambdaQueryWrapper<OperationLog> w = (LambdaQueryWrapper<OperationLog>) wrapper;
                w.and(ww -> ww.like(OperationLog::getUsername, keyword)
                        .or().like(OperationLog::getModule, keyword)
                        .or().like(OperationLog::getOperation, keyword)
                        .or().like(OperationLog::getTargetType, keyword)
                        .or().like(OperationLog::getTargetId, keyword)
                        .or().like(OperationLog::getIp, keyword)
                        .or().like(OperationLog::getTraceId, keyword));
            }
        }
        if (operationType != null) {
            if (clazz == OperationLog.class) {
                ((LambdaQueryWrapper<OperationLog>) wrapper).eq(OperationLog::getOperationType, operationType);
            }
        }
        if (StringUtils.hasText(module)) {
            if (clazz == OperationLog.class) {
                ((LambdaQueryWrapper<OperationLog>) wrapper).like(OperationLog::getModule, module);
            }
        }
        if (StringUtils.hasText(username)) {
            if (clazz == OperationLog.class) {
                ((LambdaQueryWrapper<OperationLog>) wrapper).like(OperationLog::getUsername, username);
            }
        }
        if (StringUtils.hasText(targetType)) {
            if (clazz == OperationLog.class) {
                ((LambdaQueryWrapper<OperationLog>) wrapper).eq(OperationLog::getTargetType, targetType);
            }
        }
        if (StringUtils.hasText(targetId)) {
            if (clazz == OperationLog.class) {
                ((LambdaQueryWrapper<OperationLog>) wrapper).like(OperationLog::getTargetId, targetId);
            }
        }
        if (status != null) {
            if (clazz == OperationLog.class) {
                ((LambdaQueryWrapper<OperationLog>) wrapper).eq(OperationLog::getStatus, status);
            }
        }
        if (startTime != null) {
            if (clazz == OperationLog.class) {
                ((LambdaQueryWrapper<OperationLog>) wrapper).ge(OperationLog::getCreateTime, startTime);
            }
        }
        if (endTime != null) {
            if (clazz == OperationLog.class) {
                ((LambdaQueryWrapper<OperationLog>) wrapper).le(OperationLog::getCreateTime, endTime);
            }
        }
    }

    private void applyQueryConditionWarm(LambdaQueryWrapper<OperationLogWarm> wrapper, String keyword, Integer operationType,
                                         String module, String username, String targetType, String targetId,
                                         Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(OperationLogWarm::getUsername, keyword)
                    .or().like(OperationLogWarm::getModule, keyword)
                    .or().like(OperationLogWarm::getOperation, keyword)
                    .or().like(OperationLogWarm::getTargetType, keyword)
                    .or().like(OperationLogWarm::getTargetId, keyword)
                    .or().like(OperationLogWarm::getIp, keyword)
                    .or().like(OperationLogWarm::getTraceId, keyword));
        }
        if (operationType != null) wrapper.eq(OperationLogWarm::getOperationType, operationType);
        if (StringUtils.hasText(module)) wrapper.like(OperationLogWarm::getModule, module);
        if (StringUtils.hasText(username)) wrapper.like(OperationLogWarm::getUsername, username);
        if (StringUtils.hasText(targetType)) wrapper.eq(OperationLogWarm::getTargetType, targetType);
        if (StringUtils.hasText(targetId)) wrapper.like(OperationLogWarm::getTargetId, targetId);
        if (status != null) wrapper.eq(OperationLogWarm::getStatus, status);
        if (startTime != null) wrapper.ge(OperationLogWarm::getCreateTime, startTime);
        if (endTime != null) wrapper.le(OperationLogWarm::getCreateTime, endTime);
    }

    private void applyQueryConditionArchive(LambdaQueryWrapper<OperationLogArchive> wrapper, String keyword, Integer operationType,
                                            String module, String username, String targetType, String targetId,
                                            Integer status, LocalDateTime startTime, LocalDateTime endTime) {
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(OperationLogArchive::getUsername, keyword)
                    .or().like(OperationLogArchive::getModule, keyword)
                    .or().like(OperationLogArchive::getOperation, keyword)
                    .or().like(OperationLogArchive::getTargetType, keyword)
                    .or().like(OperationLogArchive::getTargetId, keyword)
                    .or().like(OperationLogArchive::getIp, keyword)
                    .or().like(OperationLogArchive::getTraceId, keyword));
        }
        if (operationType != null) wrapper.eq(OperationLogArchive::getOperationType, operationType);
        if (StringUtils.hasText(module)) wrapper.like(OperationLogArchive::getModule, module);
        if (StringUtils.hasText(username)) wrapper.like(OperationLogArchive::getUsername, username);
        if (StringUtils.hasText(targetType)) wrapper.eq(OperationLogArchive::getTargetType, targetType);
        if (StringUtils.hasText(targetId)) wrapper.like(OperationLogArchive::getTargetId, targetId);
        if (status != null) wrapper.eq(OperationLogArchive::getStatus, status);
        if (startTime != null) wrapper.ge(OperationLogArchive::getCreateTime, startTime);
        if (endTime != null) wrapper.le(OperationLogArchive::getCreateTime, endTime);
    }

    private Map<String, Object> convertLogToMap(OperationLog log, String level) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", log.getId());
        map.put("storageLevel", level);
        map.put("storageLevelName", "热表(最近7天)");
        map.put("userId", log.getUserId());
        map.put("username", log.getUsername());
        map.put("module", log.getModule());
        map.put("operation", log.getOperation());
        map.put("operationType", log.getOperationType());
        map.put("targetType", log.getTargetType());
        map.put("targetId", log.getTargetId());
        map.put("method", log.getMethod());
        map.put("ip", log.getIp());
        map.put("status", log.getStatus());
        map.put("errorMsg", log.getErrorMsg());
        map.put("traceId", log.getTraceId());
        map.put("createTime", log.getCreateTime());
        map.put("archiveStatus", log.getArchiveStatus());
        map.put("archiveBatchId", log.getArchiveBatchId());
        return map;
    }

    private Map<String, Object> convertWarmToMap(OperationLogWarm w) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", w.getId());
        map.put("storageLevel", "WARM");
        map.put("storageLevelName", "温表(7天-3个月)");
        map.put("userId", w.getUserId());
        map.put("username", w.getUsername());
        map.put("module", w.getModule());
        map.put("operation", w.getOperation());
        map.put("operationType", w.getOperationType());
        map.put("targetType", w.getTargetType());
        map.put("targetId", w.getTargetId());
        map.put("method", w.getMethod());
        map.put("ip", w.getIp());
        map.put("status", w.getStatus());
        map.put("errorMsg", w.getErrorMsg());
        map.put("traceId", w.getTraceId());
        map.put("createTime", w.getCreateTime());
        map.put("archivedTime", w.getArchivedTime());
        map.put("archiveBatchId", w.getArchiveBatchId());
        return map;
    }

    private Map<String, Object> convertArchiveToMap(OperationLogArchive c) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        Integer sl = c.getStorageLevel();
        map.put("storageLevel", sl != null && sl == 4 ? "FILE" : "COLD");
        map.put("storageLevelName", sl != null && sl == 4 ? "文件归档(3个月以上)" : "冷表(3个月以上)");
        map.put("userId", c.getUserId());
        map.put("username", c.getUsername());
        map.put("module", c.getModule());
        map.put("operation", c.getOperation());
        map.put("operationType", c.getOperationType());
        map.put("targetType", c.getTargetType());
        map.put("targetId", c.getTargetId());
        map.put("method", c.getMethod());
        map.put("ip", c.getIp());
        map.put("status", c.getStatus());
        map.put("errorMsg", c.getErrorMsg());
        map.put("traceId", c.getTraceId());
        map.put("createTime", c.getCreateTime());
        map.put("archivedTime", c.getArchivedTime());
        map.put("archiveBatchId", c.getArchiveBatchId());
        map.put("filePath", c.getFilePath());
        return map;
    }

    private OperationLog convertWarmToLog(OperationLogWarm w) {
        OperationLog log = new OperationLog();
        log.setId(w.getId());
        log.setUserId(w.getUserId());
        log.setUsername(w.getUsername());
        log.setModule(w.getModule());
        log.setOperation(w.getOperation());
        log.setMethod(w.getMethod());
        log.setParams(w.getParams());
        log.setIp(w.getIp());
        log.setStatus(w.getStatus());
        log.setErrorMsg(w.getErrorMsg());
        log.setOperationType(w.getOperationType());
        log.setTargetType(w.getTargetType());
        log.setTargetId(w.getTargetId());
        log.setBeforeState(w.getBeforeState());
        log.setAfterState(w.getAfterState());
        log.setUserAgent(w.getUserAgent());
        log.setTraceId(w.getTraceId());
        log.setChecksum(w.getChecksum());
        log.setPreviousChecksum(w.getPreviousChecksum());
        log.setCreateTime(w.getCreateTime());
        log.setArchiveStatus(1);
        log.setArchiveBatchId(w.getArchiveBatchId());
        return log;
    }

    private OperationLog convertArchiveToLog(OperationLogArchive c) {
        OperationLog log = new OperationLog();
        log.setId(c.getId());
        log.setUserId(c.getUserId());
        log.setUsername(c.getUsername());
        log.setModule(c.getModule());
        log.setOperation(c.getOperation());
        log.setMethod(c.getMethod());
        log.setParams(c.getParams());
        log.setIp(c.getIp());
        log.setStatus(c.getStatus());
        log.setErrorMsg(c.getErrorMsg());
        log.setOperationType(c.getOperationType());
        log.setTargetType(c.getTargetType());
        log.setTargetId(c.getTargetId());
        log.setBeforeState(c.getBeforeState());
        log.setAfterState(c.getAfterState());
        log.setUserAgent(c.getUserAgent());
        log.setTraceId(c.getTraceId());
        log.setChecksum(c.getChecksum());
        log.setPreviousChecksum(c.getPreviousChecksum());
        log.setCreateTime(c.getCreateTime());
        log.setArchiveStatus(c.getStorageLevel() != null && c.getStorageLevel() == 4 ? 3 : 2);
        log.setArchiveBatchId(c.getArchiveBatchId());
        return log;
    }
}
