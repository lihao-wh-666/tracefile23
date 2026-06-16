package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.annotation.Log;
import com.exam.common.Result;
import com.exam.entity.OperationLog;
import com.exam.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/operation-log")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('1')")
    @Log(module = "操作日志", operation = "分页查询操作日志", operationType = 4, targetType = "operationLog")
    public Result<IPage<OperationLog>> page(@RequestParam(defaultValue = "1") Integer current,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Integer operationType,
                                            @RequestParam(required = false) String module,
                                            @RequestParam(required = false) String username,
                                            @RequestParam(required = false) String targetType,
                                            @RequestParam(required = false) String targetId,
                                            @RequestParam(required = false) Integer status,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        LocalDateTime startTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;
        return Result.ok(operationLogService.page(current, size, keyword, operationType,
                module, username, targetType, targetId, status, startTime, endTime));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('1')")
    @Log(module = "操作日志", operation = "查看日志详情", operationType = 4, targetType = "operationLog")
    public Result<OperationLog> getById(@PathVariable Long id) {
        return Result.ok(operationLogService.getDetail(id));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('1')")
    @Log(module = "操作日志", operation = "查看日志统计", operationType = 4, targetType = "operationLog")
    public Result<Map<String, Object>> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        LocalDateTime startTime = startDate != null ? startDate.atStartOfDay() : LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime endTime = endDate != null ? endDate.atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);
        return Result.ok(operationLogService.getStatistics(startTime, endTime));
    }

    @GetMapping("/verify-integrity")
    @PreAuthorize("hasAnyRole('1')")
    @Log(module = "操作日志", operation = "校验日志完整性", operationType = 4, targetType = "operationLog")
    public Result<Map<String, Object>> verifyIntegrity(@RequestParam(required = false) Long startId,
                                                       @RequestParam(required = false) Long endId) {
        if (startId == null) startId = 1L;
        if (endId == null) {
            endId = Long.MAX_VALUE;
        }
        return Result.ok(operationLogService.verifyIntegrity(startId, endId));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('1')")
    @Log(module = "操作日志", operation = "导出操作日志", operationType = 7, targetType = "operationLog")
    public void export(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer operationType,
                       @RequestParam(required = false) String module,
                       @RequestParam(required = false) String username,
                       @RequestParam(required = false) String targetType,
                       @RequestParam(required = false) Integer status,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                       @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                       HttpServletResponse response) throws Exception {
        LocalDateTime startTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        List<OperationLog> list = operationLogService.listForExport(keyword, operationType,
                module, username, targetType, status, startTime, endTime);

        String fileName = "operation_log_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()));

        try (OutputStream out = response.getOutputStream()) {
            byte[] BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            out.write(BOM);

            StringBuilder header = new StringBuilder();
            header.append("ID,用户ID,用户名,模块,操作,操作类型,操作对象,对象ID,IP地址,状态,错误信息,链路ID,创建时间\n");
            out.write(header.toString().getBytes(StandardCharsets.UTF_8));

            String[] opTypes = {"", "新增", "修改", "删除", "查询", "登录", "登出", "导出", "导入", "其他"};

            for (OperationLog log : list) {
                StringBuilder row = new StringBuilder();
                row.append(log.getId()).append(",");
                row.append(log.getUserId() == null ? "" : log.getUserId()).append(",");
                row.append(escapeCsv(log.getUsername())).append(",");
                row.append(escapeCsv(log.getModule())).append(",");
                row.append(escapeCsv(log.getOperation())).append(",");
                row.append(log.getOperationType() == null ? "" : opTypes[log.getOperationType()]).append(",");
                row.append(escapeCsv(log.getTargetType())).append(",");
                row.append(escapeCsv(log.getTargetId())).append(",");
                row.append(escapeCsv(log.getIp())).append(",");
                row.append(log.getStatus() != null && log.getStatus() == 1 ? "成功" : "失败").append(",");
                row.append(escapeCsv(log.getErrorMsg())).append(",");
                row.append(escapeCsv(log.getTraceId())).append(",");
                row.append(log.getCreateTime() == null ? "" : log.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
                out.write(row.toString().getBytes(StandardCharsets.UTF_8));
            }
            out.flush();
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        value = value.replace("\"", "\"\"");
        if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
            value = "\"" + value + "\"";
        }
        return value;
    }
}
