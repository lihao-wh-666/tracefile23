USE exam_db;

CREATE TABLE IF NOT EXISTS `system_config` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `config_key`   VARCHAR(100) NOT NULL COMMENT '配置键名',
    `config_value` VARCHAR(500) DEFAULT NULL COMMENT '配置值',
    `config_name`  VARCHAR(200) NOT NULL COMMENT '配置名称',
    `description`  VARCHAR(500) DEFAULT NULL COMMENT '配置描述',
    `value_type`   TINYINT      NOT NULL DEFAULT 1 COMMENT '值类型：1字符串 2整数 3布尔 4JSON',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统配置表';

INSERT IGNORE INTO `system_config` (`config_key`, `config_value`, `config_name`, `description`, `value_type`) VALUES
('login.timeout.minutes', '30', '登录超时时间(分钟)', '用户无操作超过该时间将自动登出，单位：分钟', 2);
