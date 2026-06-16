package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.annotation.Log;
import com.exam.common.Result;
import com.exam.entity.LogArchiveTask;
import com.exam.entity.LogStoragePolicy;
import com.exam.entity.OperationLogArchive;
import com.exam.service.LogArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/log-archive")
public class LogArchiveController {

    @Autowired
    private LogArchiveService logArchiveService;

    @GetMapping("/policy")
    @PreAuthorize("hasAnyRole('1')")
    @Log(module = "日志归档", operation = "查询存储策略", operationType = 4, targetType = "logStoragePolicy")
    public Result<LogStoragePolicy> getStoragePolicy() {
        return Result.ok(logArchiveService.getStoragePolicy());
    }

    @PutMapping("/policy")
    @PreAuthorize("hasAnyRole('1')")
    @Log(module = "日志归档", operation = "更新存储策略", operationType = 2, targetType = "logStoragePolicy")
    public Result<LogStoragePolicy> updateStoragePolicy(@RequestBody LogStoragePolicy policy) {
        return Result.ok(logArchiveService.updateStoragePolicy(policy));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('1')")
    @Log(module = "日志归档", operation = "查询存储统计", operationType = 4, targetType = "logArchive")
    public Result<Map<String, Object>> getStorageStatistics() {
        return Result.ok(logArchiveService.getStorageStatistics());
    }

    @PostMapping("/task")
    @PreAuthorize("hasAnyRole('1')")
    @Log(module = "日志归档", operation = "创建归档任务", operationType = 1, targetType = "logArchiveTask")
    public Result<Map<String, Object>> createArchiveTask(
            @RequestParam Integer taskType,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        Long operatorId = getCurrentUserId();
        String operatorName = getCurrentUsername();

        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);

        String batchId = logArchiveService.createArchiveTask(taskType, startTime, endTime, operatorId, operatorName);
        return Result.ok(Map.of("batchId", batchId, "message", "归档任务创建成功"));
    }

    @PostMapping("/task/{batchId}/execute")
    @PreAuthorize("hasAnyRole('1')")
    @Log(module = "日志归档", operation = "执行归档任务", operationType = 1, targetType = "logArchiveTask")
    public Result<Map<String, Object>> executeArchiveTask(@PathVariable String batchId) {
        Map<String, Object> result = logArchiveService.executeArchiveTask(batchId);
        return Result.ok(result);
    }

    @GetMapping("/task")
    @PreAuthorize("hasAnyRole('1')")
    @Log(module = "日志归档", operation = "分页查询归档任务", operationType = 4, targetType = "logArchiveTask")
    public Result<IPage<LogArchiveTask>> listArchiveTasks(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer taskType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        LocalDateTime startTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        return Result.ok(logArchiveService.listArchiveTasks(current, size, taskType, status, startTime, endTime));
    }

    @GetMapping("/task/{batchId}")
    @PreAuthorize("hasAnyRole('1')")
    @Log(module = "日志归档", operation = "查询归档任务详情", operationType = 4, targetType = "logArchiveTask")
    public Result<LogArchiveTask> getArchiveTaskDetail(@PathVariable String batchId) {
        return Result.ok(logArchiveService.getArchiveTaskDetail(batchId));
    }

    @GetMapping("/verify/{batchId}")
    @PreAuthorize("hasAnyRole('1')")
    @Log(module = "日志归档", operation = "校验归档完整性", operationType = 4, targetType = "logArchiveTask")
    public Result<Map<String, Object>> verifyArchiveIntegrity(@PathVariable String batchId) {
        return Result.ok(logArchiveService.verifyArchiveIntegrity(batchId));
    }

    @GetMapping("/trace")
    @PreAuthorize("hasAnyRole('1')")
    @Log(module = "日志归档", operation = "跨层级日志溯源", operationType = 4, targetType = "operationLog")
    public Result<Map<String, Object>> traceLogAcrossLevels(
            @RequestParam(required = false) String traceId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String targetId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        LocalDateTime startTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        return Result.ok(logArchiveService.traceLogAcrossLevels(traceId, userId, targetType, targetId, startTime, endTime));
    }

    @GetMapping("/export/{batchId}")
    @PreAuthorize("hasAnyRole('1')")
    @Log(module = "日志归档", operation = "导出归档日志", operationType = 7, targetType = "logArchiveTask")
    public void exportArchivedLogs(@PathVariable String batchId, HttpServletResponse response) throws Exception {
        List<OperationLogArchive> list = logArchiveService.listArchivedLogsForExport(batchId);

        String fileName = "archive_log_" + batchId + ".csv";
        response.setContentType("text/csv;charset=UTF-8");
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encodedFileName);

        try (OutputStream out = response.getOutputStream()) {
            byte[] BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            out.write(BOM);

            StringBuilder header = new StringBuilder();
            header.append("ID,存储层级,用户ID,用户名,模块,操作,操作类型,操作对象,对象ID,IP地址,状态,链路ID,创建时间,归档批次,归档时间\n");
            out.write(header.toString().getBytes(StandardCharsets.UTF_8));

            String[] opTypes = {"", "新增", "修改", "删除", "查询", "登录", "登出", "导出", "导入", "其他"};
            String[] levels = {"", "热表", "温表", "冷表", "文件"};

            for (OperationLogArchive log : list) {
                StringBuilder row = new StringBuilder();
                row.append(log.getId()).append(",");
                row.append(log.getStorageLevel() == null ? "" : levels[log.getStorageLevel()]).append(",");
                row.append(log.getUserId() == null ? "" : log.getUserId()).append(",");
                row.append(escapeCsv(log.getUsername())).append(",");
                row.append(escapeCsv(log.getModule())).append(",");
                row.append(escapeCsv(log.getOperation())).append(",");
                row.append(log.getOperationType() == null ? "" : opTypes[log.getOperationType()]).append(",");
                row.append(escapeCsv(log.getTargetType())).append(",");
                row.append(escapeCsv(log.getTargetId())).append(",");
                row.append(escapeCsv(log.getIp())).append(",");
                row.append(log.getStatus() != null && log.getStatus() == 1 ? "成功" : "失败").append(",");
                row.append(escapeCsv(log.getTraceId())).append(",");
                row.append(log.getCreateTime() == null ? ""
                        : log.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append(",");
                row.append(escapeCsv(log.getArchiveBatchId())).append(",");
                row.append(log.getArchivedTime() == null ? ""
                        : log.getArchivedTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
                out.write(row.toString().getBytes(StandardCharsets.UTF_8));
            }
            out.flush();
        }
    }

    @PostMapping("/auto-run")
    @PreAuthorize("hasAnyRole('1')")
    @Log(module = "日志归档", operation = "手动触发自动归档", operationType = 1, targetType = "logArchive")
    public Result<Map<String, Object>> manualTriggerAutoArchive() {
        Long operatorId = getCurrentUserId();
        String operatorName = getCurrentUsername();

        LogStoragePolicy policy = logArchiveService.getStoragePolicy();
        int hotDays = policy.getHotDays() != null ? policy.getHotDays() : 7;
        int warmDays = policy.getWarmDays() != null ? policy.getWarmDays() : 90;

        Map<String, Object> result = new java.util.HashMap<>();

        LocalDate htwEnd = LocalDate.now().minusDays(hotDays);
        LocalDate htwStart = htwEnd.minusDays(1);
        String htwBatch = logArchiveService.createArchiveTask(1,
                htwStart.atStartOfDay(), htwEnd.atStartOfDay(), operatorId, operatorName);
        Map<String, Object> htwResult = logArchiveService.executeArchiveTask(htwBatch);
        result.put("hotToWarm", htwResult);

        LocalDate wtcEnd = LocalDate.now().minusDays(warmDays);
        LocalDate wtcStart = wtcEnd.minusDays(1);
        String wtcBatch = logArchiveService.createArchiveTask(2,
                wtcStart.atStartOfDay(), wtcEnd.atStartOfDay(), operatorId, operatorName);
        Map<String, Object> wtcResult = logArchiveService.executeArchiveTask(wtcBatch);
        result.put("warmToCold", wtcResult);

        if (policy.getFileExportEnabled() != null && policy.getFileExportEnabled() == 1) {
            int coldDays = policy.getColdDays() != null ? policy.getColdDays() : 1095;
            LocalDate ctfEnd = LocalDate.now().minusDays(coldDays);
            LocalDate ctfStart = ctfEnd.minusDays(1);
            String ctfBatch = logArchiveService.createArchiveTask(3,
                    ctfStart.atStartOfDay(), ctfEnd.atStartOfDay(), operatorId, operatorName);
            Map<String, Object> ctfResult = logArchiveService.executeArchiveTask(ctfBatch);
            result.put("coldToFile", ctfResult);
        }

        result.put("message", "手动归档执行完成");
        return Result.ok(result);
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        value = value.replace("\"", "\"\"");
        if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
            value = "\"" + value + "\"";
        }
        return value;
    }

    private Long getCurrentUserId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal != null && !"anonymousUser".equals(principal.toString())) {
                return Long.parseLong(principal.toString());
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String getCurrentUsername() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal != null && !"anonymousUser".equals(principal.toString())) {
                return principal.toString();
            }
        } catch (Exception ignored) {
        }
        return "SYSTEM";
    }
}
