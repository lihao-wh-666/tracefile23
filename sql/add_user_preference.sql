CREATE TABLE `user_preference` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`      BIGINT       NOT NULL COMMENT '用户ID',
    `theme`        VARCHAR(20)  NOT NULL DEFAULT 'light' COMMENT '主题：light亮色 dark暗色',
    `language`     VARCHAR(10)  NOT NULL DEFAULT 'zh-CN' COMMENT '语言：zh-CN简体中文 en-US英语 ja-JP日语',
    `sidebar_collapsed` TINYINT NOT NULL DEFAULT 0 COMMENT '侧边栏是否折叠：0否 1是',
    `extra_config` TEXT        DEFAULT NULL COMMENT '扩展配置(JSON格式，预留)',
    `create_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户偏好设置表';
