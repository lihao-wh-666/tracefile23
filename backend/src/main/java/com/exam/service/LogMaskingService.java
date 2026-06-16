package com.exam.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface LogMaskingService {

    Map<String, Object> maskLogContent(String content, String format);

    Map<String, Object> maskLogFile(MultipartFile file, String format);

    Map<String, Object> compareLogs(String original, String masked);

    Map<String, Object> getConfig();

    byte[] getMaskedFileContent(String taskId);

    String generateTaskId();
}
