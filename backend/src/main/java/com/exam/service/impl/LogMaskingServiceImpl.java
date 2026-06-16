package com.exam.service.impl;

import com.exam.service.LogMaskingService;
import com.exam.util.LogMaskingUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LogMaskingServiceImpl implements LogMaskingService {

    private final Map<String, byte[]> maskedFileCache = new ConcurrentHashMap<>();
    private final Map<String, String> originalContentCache = new ConcurrentHashMap<>();
    private final Map<String, String> maskedContentCache = new ConcurrentHashMap<>();

    @Override
    public Map<String, Object> maskLogContent(String content, String format) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (!StringUtils.hasText(content)) {
                result.put("success", false);
                result.put("message", "日志内容不能为空");
                return result;
            }

            String actualFormat = format;
            if (!StringUtils.hasText(format) || "auto".equalsIgnoreCase(format)) {
                actualFormat = LogMaskingUtil.detectFormat(content);
            }

            String maskedContent = LogMaskingUtil.maskLog(content, actualFormat);

            String taskId = generateTaskId();
            originalContentCache.put(taskId, content);
            maskedContentCache.put(taskId, maskedContent);

            Map<String, Object> comparison = LogMaskingUtil.compareLogs(content, maskedContent);

            result.put("success", true);
            result.put("taskId", taskId);
            result.put("originalContent", content);
            result.put("maskedContent", maskedContent);
            result.put("detectedFormat", actualFormat);
            result.put("originalSize", content.length());
            result.put("maskedSize", maskedContent.length());
            result.put("comparison", comparison);
            result.put("processTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "脱敏处理失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> maskLogFile(MultipartFile file, String format) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (file == null || file.isEmpty()) {
                result.put("success", false);
                result.put("message", "文件不能为空");
                return result;
            }

            String originalContent;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
                originalContent = sb.toString();
            }

            String actualFormat = format;
            if (!StringUtils.hasText(format) || "auto".equalsIgnoreCase(format)) {
                actualFormat = LogMaskingUtil.detectFormat(originalContent);
            }

            String maskedContent = LogMaskingUtil.maskLog(originalContent, actualFormat);

            String taskId = generateTaskId();
            originalContentCache.put(taskId, originalContent);
            maskedContentCache.put(taskId, maskedContent);
            maskedFileCache.put(taskId, maskedContent.getBytes(StandardCharsets.UTF_8));

            Map<String, Object> comparison = LogMaskingUtil.compareLogs(originalContent, maskedContent);

            result.put("success", true);
            result.put("taskId", taskId);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            result.put("maskedContent", maskedContent);
            result.put("detectedFormat", actualFormat);
            result.put("originalSize", originalContent.length());
            result.put("maskedSize", maskedContent.length());
            result.put("comparison", comparison);
            result.put("processTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "文件处理失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Map<String, Object> compareLogs(String original, String masked) {
        return LogMaskingUtil.compareLogs(original, masked);
    }

    @Override
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("supportedFormats", LogMaskingUtil.getSupportedFormats());
        config.put("sensitiveKeys", LogMaskingUtil.getSensitiveKeys());
        config.put("maskString", "***");
        config.put("description", "日志敏感信息脱敏工具，支持密码、密钥、令牌等敏感信息的自动检测和脱敏");
        return config;
    }

    @Override
    public byte[] getMaskedFileContent(String taskId) {
        return maskedFileCache.get(taskId);
    }

    @Override
    public String generateTaskId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
