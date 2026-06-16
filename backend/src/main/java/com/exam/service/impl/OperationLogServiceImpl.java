package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.entity.OperationLog;
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
    private final UserMapper userMapper;
    private final QuestionMapper questionMapper;
    private final PaperMapper paperMapper;
    private final ExamMapper examMapper;
    private final SubjectMapper subjectMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final ObjectMapper objectMapper;

    public OperationLogServiceImpl(OperationLogMapper operationLogMapper,
                                   UserMapper userMapper,
                                   QuestionMapper questionMapper,
                                   PaperMapper paperMapper,
                                   ExamMapper examMapper,
                                   SubjectMapper subjectMapper,
                                   SystemConfigMapper systemConfigMapper,
                                   ObjectMapper objectMapper) {
        this.operationLogMapper = operationLogMapper;
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
        String lastChecksum = operationLogMapper.selectLastChecksum();
        log.setPreviousChecksum(lastChecksum);
        String checksum = computeChecksum(log);
        log.setChecksum(checksum);
        operationLogMapper.insert(log);
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
    }

    @Override
    public OperationLog getDetail(Long id) {
        return operationLogMapper.selectById(id);
    }

    @Override
    public Map<String, Object> verifyIntegrity(Long startId, Long endId) {
        Map<String, Object> result = new HashMap<>();
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
        return result;
    }

    @Override
    public Map<String, Object> getStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> result = new HashMap<>();
        String startStr = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endStr = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        result.put("operationTypeStats", operationLogMapper.selectOperationTypeStats(startStr, endStr));
        result.put("moduleStats", operationLogMapper.selectModuleStats(startStr, endStr));
        result.put("userStats", operationLogMapper.selectUserStats(startStr, endStr));
        result.put("dateStats", operationLogMapper.selectDateStats(startStr, endStr));
        result.put("statusStats", operationLogMapper.selectStatusStats(startStr, endStr));

        long totalCount = 0;
        long successCount = 0;
        long failCount = 0;
        List<Map<String, Object>> statusStats = operationLogMapper.selectStatusStats(startStr, endStr);
        for (Map<String, Object> stat : statusStats) {
            long cnt = ((Number) stat.get("cnt")).longValue();
            Integer status = ((Number) stat.get("status")).intValue();
            totalCount += cnt;
            if (status == 1) successCount = cnt;
            else failCount = cnt;
        }
        result.put("totalCount", totalCount);
        result.put("successCount", successCount);
        result.put("failCount", failCount);

        return result;
    }

    @Override
    public List<OperationLog> listForExport(String keyword, Integer operationType,
                                            String module, String username, String targetType,
                                            Integer status, LocalDateTime startTime, LocalDateTime endTime) {
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
    }
}
