USE exam_db;

CREATE TABLE IF NOT EXISTS `exam_switch_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `record_id` BIGINT NOT NULL COMMENT '考试记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `exam_id` BIGINT NOT NULL COMMENT '考试ID',
    `switch_type` TINYINT NOT NULL COMMENT '切屏类型：1标签页切换 2窗口切换 3应用切换 4截图操作 5屏幕录制',
    `switch_time` DATETIME NOT NULL COMMENT '切屏发生时间',
    `duration` INT DEFAULT 0 COMMENT '切屏持续时长(秒)',
    `app_name` VARCHAR(200) DEFAULT NULL COMMENT '涉及的应用程序名称',
    `screenshot_detected` TINYINT DEFAULT 0 COMMENT '是否检测到截图：0否 1是',
    `screen_record_detected` TINYINT DEFAULT 0 COMMENT '是否检测到录屏：0否 1是',
    `details` TEXT DEFAULT NULL COMMENT '详细信息(JSON格式)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_record_id` (`record_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_exam_id` (`exam_id`),
    INDEX `idx_switch_time` (`switch_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试切屏记录表';

ALTER TABLE `exam_record` 
ADD COLUMN `switch_count` INT DEFAULT 0 COMMENT '切屏总次数' AFTER `total_pause_time`,
ADD COLUMN `total_switch_duration` INT DEFAULT 0 COMMENT '切屏总时长(秒)' AFTER `switch_count`,
ADD COLUMN `screenshot_count` INT DEFAULT 0 COMMENT '截图检测次数' AFTER `total_switch_duration`,
ADD COLUMN `screen_record_count` INT DEFAULT 0 COMMENT '录屏检测次数' AFTER `screenshot_count`,
ADD COLUMN `warning_count` INT DEFAULT 0 COMMENT '警告次数' AFTER `screen_record_count`;

INSERT INTO `system_config` (`config_key`, `config_value`, `description`, `create_time`, `update_time`) VALUES
('exam.max_switch_count', '3', '考试最大允许切屏次数', NOW(), NOW()),
('exam.max_single_switch_duration', '30', '单次切屏最大允许时长(秒)', NOW(), NOW()),
('exam.max_total_switch_duration', '60', '切屏累计最大允许时长(秒)', NOW(), NOW()),
('exam.screenshot_detection_enabled', 'true', '是否启用截图检测', NOW(), NOW()),
('exam.screen_record_detection_enabled', 'true', '是否启用录屏检测', NOW(), NOW()),
('exam.auto_submit_on_exceed', 'false', '超过阈值是否自动交卷', NOW(), NOW())
ON DUPLICATE KEY UPDATE `config_value` = VALUES(`config_value`), `update_time` = NOW();
