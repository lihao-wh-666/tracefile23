package com.exam.schedule;

import com.exam.entity.LogArchiveTask;
import com.exam.entity.LogStoragePolicy;
import com.exam.service.LogArchiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

@Component
@EnableScheduling
public class LogArchiveScheduleTask {

    private static final Logger log = LoggerFactory.getLogger(LogArchiveScheduleTask.class);

    private final LogArchiveService logArchiveService;

    public LogArchiveScheduleTask(LogArchiveService logArchiveService) {
        this.logArchiveService = logArchiveService;
    }

    @Scheduled(cron = "${log.archive.cron:0 0 2 * * ?}")
    public void autoArchive() {
        log.info("=== 开始执行日志自动归档任务 ===");
        try {
            LogStoragePolicy policy = logArchiveService.getStoragePolicy();
            if (policy.getAutoArchiveEnabled() == null || policy.getAutoArchiveEnabled() != 1) {
                log.info("自动归档已禁用，跳过执行");
                return;
            }

            executeHotToWarm(policy);
            executeWarmToCold(policy);
            if (policy.getFileExportEnabled() != null && policy.getFileExportEnabled() == 1) {
                executeColdToFile(policy);
            }

            log.info("=== 日志自动归档任务执行完成 ===");
        } catch (Exception e) {
            log.error("自动归档任务执行异常", e);
        }
    }

    private void executeHotToWarm(LogStoragePolicy policy) {
        log.info("--- 开始执行 热->温 归档 ---");
        try {
            int hotDays = policy.getHotDays() != null ? policy.getHotDays() : 7;

            LocalDate endDate = LocalDate.now().minusDays(hotDays);
            LocalDate startDate = endDate.minusDays(30);

            LocalDateTime startTime = startDate.atStartOfDay();
            LocalDateTime endTime = endDate.atStartOfDay();

            String batchId = logArchiveService.createArchiveTask(1, startTime, endTime, null, "SYSTEM_SCHEDULE");
            log.info("创建热->温归档任务：batchId={}, 时间范围=[{} ~ {})", batchId, startTime, endTime);

            Map<String, Object> result = logArchiveService.executeArchiveTask(batchId);
            Boolean success = (Boolean) result.get("success");
            log.info("热->温归档任务执行结果：success={}, message={}", success, result.get("message"));
        } catch (Exception e) {
            log.error("热->温归档执行异常", e);
        }
    }

    private void executeWarmToCold(LogStoragePolicy policy) {
        log.info("--- 开始执行 温->冷 归档 ---");
        try {
            int warmDays = policy.getWarmDays() != null ? policy.getWarmDays() : 90;
            int hotDays = policy.getHotDays() != null ? policy.getHotDays() : 7;

            LocalDate endDate = LocalDate.now().minusDays(warmDays);
            LocalDate startDate = endDate.minusDays(warmDays - hotDays);

            LocalDateTime startTime = startDate.atStartOfDay();
            LocalDateTime endTime = endDate.atStartOfDay();

            String batchId = logArchiveService.createArchiveTask(2, startTime, endTime, null, "SYSTEM_SCHEDULE");
            log.info("创建温->冷归档任务：batchId={}, 时间范围=[{} ~ {})", batchId, startTime, endTime);

            Map<String, Object> result = logArchiveService.executeArchiveTask(batchId);
            Boolean success = (Boolean) result.get("success");
            log.info("温->冷归档任务执行结果：success={}, message={}", success, result.get("message"));
        } catch (Exception e) {
            log.error("温->冷归档执行异常", e);
        }
    }

    private void executeColdToFile(LogStoragePolicy policy) {
        log.info("--- 开始执行 冷->文件 归档 ---");
        try {
            int coldDays = policy.getColdDays() != null ? policy.getColdDays() : 1095;
            int warmDays = policy.getWarmDays() != null ? policy.getWarmDays() : 90;

            LocalDate endDate = LocalDate.now().minusDays(coldDays);
            LocalDate startDate = endDate.minusDays(coldDays - warmDays);

            LocalDateTime startTime = startDate.atStartOfDay();
            LocalDateTime endTime = endDate.atStartOfDay();

            String batchId = logArchiveService.createArchiveTask(3, startTime, endTime, null, "SYSTEM_SCHEDULE");
            log.info("创建冷->文件归档任务：batchId={}, 时间范围=[{} ~ {})", batchId, startTime, endTime);

            Map<String, Object> result = logArchiveService.executeArchiveTask(batchId);
            Boolean success = (Boolean) result.get("success");
            log.info("冷->文件归档任务执行结果：success={}, message={}", success, result.get("message"));
        } catch (Exception e) {
            log.error("冷->文件归档执行异常", e);
        }
    }
}
