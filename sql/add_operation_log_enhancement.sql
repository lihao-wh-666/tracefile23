USE exam_db;

ALTER TABLE `operation_log`
    ADD COLUMN `operation_type` TINYINT DEFAULT NULL COMMENT '操作类型：1新增 2修改 3删除 4查询 5登录 6登出 7导出 8导入 9其他' AFTER `error_msg`,
    ADD COLUMN `target_type` VARCHAR(100) DEFAULT NULL COMMENT '操作对象类型（如：user、question、paper、exam等）' AFTER `operation_type`,
    ADD COLUMN `target_id` VARCHAR(100) DEFAULT NULL COMMENT '操作对象ID' AFTER `target_type`,
    ADD COLUMN `before_state` TEXT DEFAULT NULL COMMENT '操作前状态（JSON格式）' AFTER `target_id`,
    ADD COLUMN `after_state` TEXT DEFAULT NULL COMMENT '操作后状态（JSON格式）' AFTER `before_state`,
    ADD COLUMN `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理（浏览器信息）' AFTER `after_state`,
    ADD COLUMN `trace_id` VARCHAR(64) DEFAULT NULL COMMENT '链路追踪ID' AFTER `user_agent`,
    ADD COLUMN `checksum` VARCHAR(64) DEFAULT NULL COMMENT '数据完整性校验值（SHA-256）' AFTER `trace_id`,
    ADD COLUMN `previous_checksum` VARCHAR(64) DEFAULT NULL COMMENT '前一条记录的校验值（哈希链防篡改）' AFTER `checksum`,
    ADD INDEX `idx_operation_type` (`operation_type`),
    ADD INDEX `idx_target` (`target_type`, `target_id`),
    ADD INDEX `idx_trace_id` (`trace_id`),
    ADD INDEX `idx_module_operation` (`module`, `operation`);
