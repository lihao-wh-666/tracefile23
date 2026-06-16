USE exam_db;

-- =============================================
-- 日志分级存储和归档机制表结构
-- 分级策略：
--   HOT (热数据)  : operation_log 表，最近7天
--   WARM (温数据) : operation_log_warm 表，7天-3个月
--   COLD (冷数据) : operation_log_archive 表，3个月以上
-- =============================================

-- 1. 温数据表（存储7天~3个月的日志）
CREATE TABLE IF NOT EXISTS `operation_log_warm` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `username` VARCHAR(100) DEFAULT NULL COMMENT '用户名',
    `module` VARCHAR(100) DEFAULT NULL COMMENT '模块名称',
    `operation` VARCHAR(200) DEFAULT NULL COMMENT '操作描述',
    `method` VARCHAR(500) DEFAULT NULL COMMENT '方法名',
    `params` TEXT DEFAULT NULL COMMENT '请求参数',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `status` TINYINT DEFAULT NULL COMMENT '状态：1成功 0失败',
    `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `operation_type` TINYINT DEFAULT NULL COMMENT '操作类型：1新增 2修改 3删除 4查询 5登录 6登出 7导出 8导入 9其他',
    `target_type` VARCHAR(100) DEFAULT NULL COMMENT '操作对象类型',
    `target_id` VARCHAR(100) DEFAULT NULL COMMENT '操作对象ID',
    `before_state` TEXT DEFAULT NULL COMMENT '操作前状态',
    `after_state` TEXT DEFAULT NULL COMMENT '操作后状态',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
    `trace_id` VARCHAR(64) DEFAULT NULL COMMENT '链路追踪ID',
    `checksum` VARCHAR(64) DEFAULT NULL COMMENT '完整性校验值',
    `previous_checksum` VARCHAR(64) DEFAULT NULL COMMENT '前一条记录校验值',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `archived_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '迁移到温表的时间',
    `archive_batch_id` VARCHAR(64) DEFAULT NULL COMMENT '归档批次ID',
    PRIMARY KEY (`id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_trace_id` (`trace_id`),
    KEY `idx_module_operation` (`module`, `operation`),
    KEY `idx_archived_time` (`archived_time`),
    KEY `idx_archive_batch_id` (`archive_batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志温数据表（7天-3个月）';

-- 2. 冷数据表/归档表（存储3个月以上的日志）
CREATE TABLE IF NOT EXISTS `operation_log_archive` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
    `username` VARCHAR(100) DEFAULT NULL COMMENT '用户名',
    `module` VARCHAR(100) DEFAULT NULL COMMENT '模块名称',
    `operation` VARCHAR(200) DEFAULT NULL COMMENT '操作描述',
    `method` VARCHAR(500) DEFAULT NULL COMMENT '方法名',
    `params` TEXT DEFAULT NULL COMMENT '请求参数',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `status` TINYINT DEFAULT NULL COMMENT '状态：1成功 0失败',
    `error_msg` VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `operation_type` TINYINT DEFAULT NULL COMMENT '操作类型',
    `target_type` VARCHAR(100) DEFAULT NULL COMMENT '操作对象类型',
    `target_id` VARCHAR(100) DEFAULT NULL COMMENT '操作对象ID',
    `before_state` TEXT DEFAULT NULL COMMENT '操作前状态',
    `after_state` TEXT DEFAULT NULL COMMENT '操作后状态',
    `user_agent` VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
    `trace_id` VARCHAR(64) DEFAULT NULL COMMENT '链路追踪ID',
    `checksum` VARCHAR(64) DEFAULT NULL COMMENT '完整性校验值',
    `previous_checksum` VARCHAR(64) DEFAULT NULL COMMENT '前一条记录校验值',
    `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
    `archived_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
    `archive_batch_id` VARCHAR(64) NOT NULL COMMENT '归档批次ID',
    `storage_level` TINYINT NOT NULL DEFAULT 1 COMMENT '存储层级：1温表 2冷表 3文件',
    `file_path` VARCHAR(500) DEFAULT NULL COMMENT '文件存储路径（storage_level=3时使用）',
    PRIMARY KEY (`id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_target` (`target_type`, `target_id`),
    KEY `idx_trace_id` (`trace_id`),
    KEY `idx_archived_time` (`archived_time`),
    KEY `idx_archive_batch_id` (`archive_batch_id`),
    KEY `idx_storage_level` (`storage_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志归档表（3个月以上）';

-- 3. 归档任务记录表
CREATE TABLE IF NOT EXISTS `log_archive_task` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `batch_id` VARCHAR(64) NOT NULL COMMENT '归档批次ID',
    `task_type` TINYINT NOT NULL COMMENT '任务类型：1热->温 2温->冷 3冷->文件',
    `source_level` TINYINT NOT NULL COMMENT '源存储层级：1热表 2温表 3冷表',
    `target_level` TINYINT NOT NULL COMMENT '目标存储层级：2温表 3冷表 4文件',
    `start_time` DATETIME NOT NULL COMMENT '处理的日志起始时间',
    `end_time` DATETIME NOT NULL COMMENT '处理的日志结束时间',
    `total_count` BIGINT NOT NULL DEFAULT 0 COMMENT '总记录数',
    `success_count` BIGINT NOT NULL DEFAULT 0 COMMENT '成功迁移数',
    `fail_count` BIGINT NOT NULL DEFAULT 0 COMMENT '失败数',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待执行 1执行中 2成功 3失败',
    `error_msg` TEXT DEFAULT NULL COMMENT '错误信息',
    `file_path` VARCHAR(500) DEFAULT NULL COMMENT '文件路径（冷->文件时）',
    `file_checksum` VARCHAR(64) DEFAULT NULL COMMENT '文件校验值',
    `file_size` BIGINT DEFAULT NULL COMMENT '文件大小（字节）',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID（手动触发时）',
    `operator_name` VARCHAR(100) DEFAULT NULL COMMENT '操作人名称',
    `execute_start_time` DATETIME DEFAULT NULL COMMENT '执行开始时间',
    `execute_end_time` DATETIME DEFAULT NULL COMMENT '执行结束时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_batch_id` (`batch_id`),
    KEY `idx_task_type` (`task_type`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_time_range` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日志归档任务记录表';

-- 4. 日志存储策略配置表
CREATE TABLE IF NOT EXISTS `log_storage_policy` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `policy_name` VARCHAR(100) NOT NULL COMMENT '策略名称',
    `hot_days` INT NOT NULL DEFAULT 7 COMMENT '热数据保留天数',
    `warm_days` INT NOT NULL DEFAULT 90 COMMENT '温数据保留天数（含热数据，即总天数）',
    `cold_days` INT NOT NULL DEFAULT 1095 COMMENT '冷数据保留天数（含热+温，默认3年合规期）',
    `auto_archive_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用自动归档：1启用 0禁用',
    `archive_cron` VARCHAR(100) NOT NULL DEFAULT '0 0 2 * * ?' COMMENT '自动归档定时表达式（默认每天凌晨2点）',
    `file_export_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '是否启用文件导出归档：1启用 0禁用',
    `file_storage_path` VARCHAR(500) DEFAULT NULL COMMENT '文件存储根路径',
    `file_compress_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '文件是否压缩：1是 0否',
    `integrity_verify_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '归档时完整性校验：1启用 0禁用',
    `delete_after_archive` TINYINT NOT NULL DEFAULT 1 COMMENT '归档后是否删除源数据：1是 0否',
    `batch_size` INT NOT NULL DEFAULT 1000 COMMENT '每批处理记录数',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日志存储策略配置表';

-- 插入默认存储策略
INSERT IGNORE INTO `log_storage_policy` 
(`policy_name`, `hot_days`, `warm_days`, `cold_days`, `auto_archive_enabled`, `archive_cron`, 
 `file_export_enabled`, `file_storage_path`, `file_compress_enabled`, `integrity_verify_enabled`, 
 `delete_after_archive`, `batch_size`, `remark`)
VALUES 
('默认合规策略', 7, 90, 1095, 1, '0 0 2 * * ?', 
 0, '/data/log-archive', 1, 1, 
 1, 1000, '遵循三级等保合规要求，日志保留3年以上');

-- 5. 为operation_log主表添加归档标记字段
ALTER TABLE `operation_log` 
    ADD COLUMN IF NOT EXISTS `archive_status` TINYINT NOT NULL DEFAULT 0 COMMENT '归档状态：0未归档 1已迁移温表 2已迁移冷表 3已导出文件' AFTER `previous_checksum`,
    ADD COLUMN IF NOT EXISTS `archive_batch_id` VARCHAR(64) DEFAULT NULL COMMENT '归档批次ID' AFTER `archive_status`,
    ADD INDEX IF NOT EXISTS `idx_archive_status` (`archive_status`),
    ADD INDEX IF NOT EXISTS `idx_create_time` (`create_time`);
