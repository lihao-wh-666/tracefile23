package com.exam.controller;

import com.exam.annotation.Log;
import com.exam.common.Result;
import com.exam.service.LogMaskingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/log-masking")
public class LogMaskingController {

    @Autowired
    private LogMaskingService logMaskingService;

    @GetMapping("/config")
    @PreAuthorize("hasAnyRole('1')")
    public Result<Map<String, Object>> getConfig() {
        try {
            Map<String, Object> config = logMaskingService.getConfig();
            return Result.ok(config);
        } catch (Exception e) {
            return Result.fail("获取配置失败: " + e.getMessage());
        }
    }

    @PostMapping("/mask")
    @PreAuthorize("hasAnyRole('1')")
    @Log(module = "日志脱敏", operation = "脱敏处理文本内容", operationType = 4, targetType = "logMasking")
    public Result<Map<String, Object>> maskContent(@RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            String format = request.get("format");
            Map<String, Object> result = logMaskingService.maskLogContent(content, format);
            if (Boolean.TRUE.equals(result.get("success"))) {
                return Result.ok(result);
            } else {
                return Result.fail((String) result.get("message"));
            }
        } catch (Exception e) {
            return Result.fail("脱敏处理失败: " + e.getMessage());
        }
    }

    @PostMapping("/mask-file")
    @PreAuthorize("hasAnyRole('1')")
    @Log(module = "日志脱敏", operation = "脱敏处理日志文件", operationType = 7, targetType = "logMasking")
    public Result<Map<String, Object>> maskFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "format", required = false, defaultValue = "auto") String format) {
        try {
            Map<String, Object> result = logMaskingService.maskLogFile(file, format);
            if (Boolean.TRUE.equals(result.get("success"))) {
                return Result.ok(result);
            } else {
                return Result.fail((String) result.get("message"));
            }
        } catch (Exception e) {
            return Result.fail("文件处理失败: " + e.getMessage());
        }
    }

    @PostMapping("/compare")
    @PreAuthorize("hasAnyRole('1')")
    public Result<Map<String, Object>> compareLogs(@RequestBody Map<String, String> request) {
        try {
            String original = request.get("original");
            String masked = request.get("masked");
            Map<String, Object> result = logMaskingService.compareLogs(original, masked);
            return Result.ok(result);
        } catch (Exception e) {
            return Result.fail("对比失败: " + e.getMessage());
        }
    }

    @GetMapping("/download/{taskId}")
    @PreAuthorize("hasAnyRole('1')")
    public ResponseEntity<byte[]> downloadMaskedFile(@PathVariable String taskId) {
        try {
            byte[] content = logMaskingService.getMaskedFileContent(taskId);
            if (content == null) {
                return ResponseEntity.notFound().build();
            }

            String fileName = "masked-log-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".txt";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName)
                    .contentType(MediaType.TEXT_PLAIN)
                    .contentLength(content.length)
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/preview")
    @PreAuthorize("hasAnyRole('1')")
    public Result<Map<String, Object>> previewMasking(@RequestBody Map<String, String> request) {
        try {
            String content = request.get("content");
            String format = request.getOrDefault("format", "auto");

            String detectedFormat = format;
            if ("auto".equalsIgnoreCase(format)) {
                detectedFormat = com.exam.util.LogMaskingUtil.detectFormat(content);
            }

            String maskedContent = com.exam.util.LogMaskingUtil.maskLog(content, detectedFormat);
            Map<String, Object> comparison = com.exam.util.LogMaskingUtil.compareLogs(content, maskedContent);

            Map<String, Object> result = new java.util.HashMap<>();
            result.put("original", content);
            result.put("masked", maskedContent);
            result.put("detectedFormat", detectedFormat);
            result.put("comparison", comparison);

            return Result.ok(result);
        } catch (Exception e) {
            return Result.fail("预览失败: " + e.getMessage());
        }
    }
}
