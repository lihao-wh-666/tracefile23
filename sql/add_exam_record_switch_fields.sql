USE exam_db;

ALTER TABLE `exam_record`
    ADD COLUMN `switch_count`         INT NOT NULL DEFAULT 0 COMMENT '切屏次数' AFTER `total_pause_time`,
    ADD COLUMN `total_switch_duration` INT NOT NULL DEFAULT 0 COMMENT '累计切屏时长(秒)' AFTER `switch_count`,
    ADD COLUMN `screenshot_count`     INT NOT NULL DEFAULT 0 COMMENT '截图检测次数' AFTER `total_switch_duration`,
    ADD COLUMN `screen_record_count`  INT NOT NULL DEFAULT 0 COMMENT '录屏检测次数' AFTER `screenshot_count`,
    ADD COLUMN `warning_count`        INT NOT NULL DEFAULT 0 COMMENT '警告次数' AFTER `screen_record_count`;
