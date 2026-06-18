SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE DATABASE IF NOT EXISTS exam_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE exam_db;
SET NAMES utf8mb4;

CREATE TABLE `subject` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(100) NOT NULL,
    `description` VARCHAR(500) DEFAULT NULL,
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='科目表';

CREATE TABLE `user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `username`    VARCHAR(50)  NOT NULL,
    `password`    VARCHAR(200) NOT NULL,
    `real_name`   VARCHAR(50)  DEFAULT NULL,
    `role`        TINYINT      NOT NULL COMMENT '1管理员 2教师 3学生',
    `avatar`      VARCHAR(500) DEFAULT NULL,
    `email`       VARCHAR(100) DEFAULT NULL,
    `phone`       VARCHAR(20)  DEFAULT NULL,
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
    `login_locked` TINYINT      NOT NULL DEFAULT 0 COMMENT '登录锁定状态：0未锁定 1已锁定',
    `lock_end_time` DATETIME     DEFAULT NULL COMMENT '锁定到期时间',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

CREATE TABLE `question` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `subject_id`  BIGINT       NOT NULL,
    `type`        TINYINT      NOT NULL COMMENT '1单选 2多选 3判断 4填空 5问答',
    `content`     TEXT         NOT NULL,
    `option_a`    VARCHAR(500) DEFAULT NULL,
    `option_b`    VARCHAR(500) DEFAULT NULL,
    `option_c`    VARCHAR(500) DEFAULT NULL,
    `option_d`    VARCHAR(500) DEFAULT NULL,
    `answer`      VARCHAR(500) NOT NULL,
    `analysis`    TEXT         DEFAULT NULL,
    `score`       INT          NOT NULL DEFAULT 0,
    `difficulty`  TINYINT      NOT NULL DEFAULT 1 COMMENT '1简单 2中等 3困难',
    `create_by`   BIGINT       DEFAULT NULL,
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_subject_id` (`subject_id`),
    INDEX `idx_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='题目表';

CREATE TABLE `paper` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(200) NOT NULL,
    `subject_id`  BIGINT       NOT NULL,
    `total_score` INT          NOT NULL DEFAULT 0,
    `pass_score`  INT          NOT NULL DEFAULT 0,
    `duration`    INT          NOT NULL DEFAULT 0 COMMENT '分钟',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0草稿 1已发布',
    `create_by`   BIGINT       DEFAULT NULL,
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_subject_id` (`subject_id`),
    INDEX `idx_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='试卷表';

CREATE TABLE `paper_question` (
    `id`          BIGINT NOT NULL AUTO_INCREMENT,
    `paper_id`    BIGINT NOT NULL,
    `question_id` BIGINT NOT NULL,
    `sort`        INT    NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_paper_id` (`paper_id`),
    INDEX `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='试卷题目关联表';

CREATE TABLE `exam` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `paper_id`    BIGINT       NOT NULL,
    `name`        VARCHAR(200) NOT NULL,
    `start_time`  DATETIME     NOT NULL,
    `end_time`    DATETIME     NOT NULL,
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0未开始 1进行中 2已结束',
    `create_by`   BIGINT       DEFAULT NULL,
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_paper_id` (`paper_id`),
    INDEX `idx_create_by` (`create_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试表';

CREATE TABLE `exam_record` (
    `id`               BIGINT   NOT NULL AUTO_INCREMENT,
    `exam_id`          BIGINT   NOT NULL,
    `user_id`          BIGINT   NOT NULL,
    `paper_id`         BIGINT   NOT NULL,
    `start_time`       DATETIME DEFAULT CURRENT_TIMESTAMP,
    `submit_time`      DATETIME DEFAULT NULL,
    `score`            INT      NOT NULL DEFAULT 0,
    `status`           TINYINT  NOT NULL DEFAULT 0 COMMENT '0考试中 1已提交 2已批改 3已暂停',
    `duration`         INT      NOT NULL DEFAULT 0 COMMENT '秒',
    `pause_count`      INT      NOT NULL DEFAULT 0 COMMENT '暂停次数',
    `total_pause_time` INT      NOT NULL DEFAULT 0 COMMENT '累计暂停时长(秒)',
    `question_order`   VARCHAR(1000) DEFAULT NULL COMMENT '题目随机顺序(逗号分隔的question_id)',
    `last_pause_time`  DATETIME DEFAULT NULL COMMENT '最后暂停时间',
    `last_resume_time` DATETIME DEFAULT NULL COMMENT '最后恢复时间',
    `update_time`      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_exam_id` (`exam_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_paper_id` (`paper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试记录表';

CREATE TABLE `exam_answer` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `record_id`   BIGINT       NOT NULL,
    `question_id` BIGINT       NOT NULL,
    `answer`      VARCHAR(500) DEFAULT NULL,
    `option_order` VARCHAR(20) DEFAULT NULL COMMENT '选项随机顺序(如BCAD)',
    `is_correct`  TINYINT      NOT NULL DEFAULT 0 COMMENT '0错 1对 2半对',
    `score`       INT          NOT NULL DEFAULT 0,
    `auto_score`  INT          DEFAULT NULL,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_record_id` (`record_id`),
    INDEX `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试答案表';

CREATE TABLE `operation_log` (
    `id`                 BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`            BIGINT       DEFAULT NULL,
    `username`           VARCHAR(50)  DEFAULT NULL,
    `module`             VARCHAR(100) DEFAULT NULL,
    `operation`          VARCHAR(200) DEFAULT NULL,
    `method`             VARCHAR(200) DEFAULT NULL,
    `params`             TEXT         DEFAULT NULL,
    `ip`                 VARCHAR(50)  DEFAULT NULL,
    `status`             TINYINT      NOT NULL DEFAULT 1,
    `error_msg`          VARCHAR(500) DEFAULT NULL,
    `operation_type`     TINYINT      DEFAULT NULL COMMENT '1新增 2修改 3删除 4查询 5登录 6登出 7导出 8导入 9其他',
    `target_type`        VARCHAR(100) DEFAULT NULL COMMENT '操作对象类型',
    `target_id`          VARCHAR(100) DEFAULT NULL COMMENT '操作对象ID',
    `before_state`       TEXT         DEFAULT NULL COMMENT '操作前状态JSON',
    `after_state`        TEXT         DEFAULT NULL COMMENT '操作后状态JSON',
    `user_agent`         VARCHAR(500) DEFAULT NULL COMMENT '浏览器UA',
    `trace_id`           VARCHAR(64)  DEFAULT NULL COMMENT '链路追踪ID',
    `checksum`           VARCHAR(64)  DEFAULT NULL COMMENT 'SHA-256哈希值',
    `previous_checksum`  VARCHAR(64)  DEFAULT NULL COMMENT '前一条哈希值(链式校验)',
    `archive_status`     TINYINT      NOT NULL DEFAULT 0 COMMENT '归档状态：0未归档 1已迁移温表 2已迁移冷表 3已导出文件',
    `archive_batch_id`   VARCHAR(64)  DEFAULT NULL COMMENT '归档批次ID',
    `create_time`        DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_target` (`target_type`, `target_id`),
    INDEX `idx_trace_id` (`trace_id`),
    INDEX `idx_module_operation` (`module`, `operation`),
    INDEX `idx_archive_status` (`archive_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表（热数据：最近7天）';

INSERT INTO `subject` (`name`, `description`) VALUES
('高等数学', '大学高等数学课程，涵盖微积分、极限、导数、积分等核心内容'),
('大学英语', '大学英语四级考试相关课程，包含听说读写综合训练'),
('线性代数', '线性代数课程，涵盖矩阵、行列式、向量空间、线性变换等内容'),
('概率论与数理统计', '概率论与数理统计课程，包含随机事件、随机变量、统计推断等'),
('计算机基础', '计算机基础知识入门，涵盖计算机组成、操作系统、网络基础等'),
('C语言程序设计', 'C语言程序设计基础课程，学习结构化编程和算法实现'),
('数据结构', '数据结构与算法课程，涵盖链表、栈、队列、树、图等数据结构'),
('操作系统', '操作系统原理课程，学习进程管理、内存管理、文件系统等'),
('计算机网络', '计算机网络课程，涵盖TCP/IP协议、HTTP、网络安全等内容'),
('数据库原理', '数据库原理课程，学习SQL语言、关系型数据库设计与优化'),
('Java程序设计', 'Java面向对象程序设计，学习Java语法、集合框架、多线程等'),
('Python程序设计', 'Python程序设计，涵盖Python基础、数据分析、Web开发等'),
('思想政治', '思想政治教育课程，培养正确的世界观、人生观、价值观'),
('大学物理', '大学物理基础课程，涵盖力学、电磁学、光学、热学等内容'),
('大学化学', '大学化学基础课程，学习无机化学、有机化学基础知识'),
('大学体育', '大学体育课程，包含田径、球类、健身等体育运动训练');

CREATE TABLE `system_config` (
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

INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `description`, `value_type`) VALUES
('login.timeout.minutes', '30', '登录超时时间(分钟)', '用户无操作超过该时间将自动登出，单位：分钟', 2),
('login.max.error.count', '5', '登录最大错误次数', '密码错误次数达到该值后锁定账号，0表示不限制', 2),
('login.lock.duration.minutes', '30', '账号锁定时长(分钟)', '密码错误次数超限后账号锁定的时长，单位：分钟', 2);

CREATE TABLE IF NOT EXISTS `user_preference` (
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

-- =============================================
-- 日志分级存储相关表
-- =============================================

-- 温数据表（7天 ~ 3个月）
CREATE TABLE IF NOT EXISTS `operation_log_warm` (
    `id`               BIGINT       NOT NULL COMMENT '主键ID（与原表相同）',
    `user_id`          BIGINT       DEFAULT NULL COMMENT '用户ID',
    `username`         VARCHAR(100) DEFAULT NULL COMMENT '用户名',
    `module`           VARCHAR(100) DEFAULT NULL COMMENT '模块名称',
    `operation`        VARCHAR(200) DEFAULT NULL COMMENT '操作描述',
    `method`           VARCHAR(500) DEFAULT NULL COMMENT '方法名',
    `params`           TEXT         DEFAULT NULL COMMENT '请求参数',
    `ip`               VARCHAR(50)  DEFAULT NULL COMMENT 'IP地址',
    `status`           TINYINT      DEFAULT NULL COMMENT '状态：1成功 0失败',
    `error_msg`        VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `operation_type`   TINYINT      DEFAULT NULL COMMENT '操作类型：1新增 2修改 3删除 4查询 5登录 6登出 7导出 8导入 9其他',
    `target_type`      VARCHAR(100) DEFAULT NULL COMMENT '操作对象类型',
    `target_id`        VARCHAR(100) DEFAULT NULL COMMENT '操作对象ID',
    `before_state`     TEXT         DEFAULT NULL COMMENT '操作前状态',
    `after_state`      TEXT         DEFAULT NULL COMMENT '操作后状态',
    `user_agent`       VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
    `trace_id`         VARCHAR(64)  DEFAULT NULL COMMENT '链路追踪ID',
    `checksum`         VARCHAR(64)  DEFAULT NULL COMMENT '完整性校验值',
    `previous_checksum` VARCHAR(64) DEFAULT NULL COMMENT '前一条记录校验值',
    `create_time`      DATETIME     DEFAULT NULL COMMENT '创建时间',
    `archived_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '迁移到温表的时间',
    `archive_batch_id` VARCHAR(64)  DEFAULT NULL COMMENT '归档批次ID',
    PRIMARY KEY (`id`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_target` (`target_type`, `target_id`),
    INDEX `idx_trace_id` (`trace_id`),
    INDEX `idx_module_operation` (`module`, `operation`),
    INDEX `idx_archived_time` (`archived_time`),
    INDEX `idx_archive_batch_id` (`archive_batch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志温数据表（7天-3个月）';

-- 冷数据/归档表（3个月以上）
CREATE TABLE IF NOT EXISTS `operation_log_archive` (
    `id`               BIGINT       NOT NULL COMMENT '主键ID',
    `user_id`          BIGINT       DEFAULT NULL COMMENT '用户ID',
    `username`         VARCHAR(100) DEFAULT NULL COMMENT '用户名',
    `module`           VARCHAR(100) DEFAULT NULL COMMENT '模块名称',
    `operation`        VARCHAR(200) DEFAULT NULL COMMENT '操作描述',
    `method`           VARCHAR(500) DEFAULT NULL COMMENT '方法名',
    `params`           TEXT         DEFAULT NULL COMMENT '请求参数',
    `ip`               VARCHAR(50)  DEFAULT NULL COMMENT 'IP地址',
    `status`           TINYINT      DEFAULT NULL COMMENT '状态：1成功 0失败',
    `error_msg`        VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    `operation_type`   TINYINT      DEFAULT NULL COMMENT '操作类型',
    `target_type`      VARCHAR(100) DEFAULT NULL COMMENT '操作对象类型',
    `target_id`        VARCHAR(100) DEFAULT NULL COMMENT '操作对象ID',
    `before_state`     TEXT         DEFAULT NULL COMMENT '操作前状态',
    `after_state`      TEXT         DEFAULT NULL COMMENT '操作后状态',
    `user_agent`       VARCHAR(500) DEFAULT NULL COMMENT '用户代理',
    `trace_id`         VARCHAR(64)  DEFAULT NULL COMMENT '链路追踪ID',
    `checksum`         VARCHAR(64)  DEFAULT NULL COMMENT '完整性校验值',
    `previous_checksum` VARCHAR(64) DEFAULT NULL COMMENT '前一条记录校验值',
    `create_time`      DATETIME     DEFAULT NULL COMMENT '创建时间',
    `archived_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '归档时间',
    `archive_batch_id` VARCHAR(64)  NOT NULL COMMENT '归档批次ID',
    `storage_level`    TINYINT      NOT NULL DEFAULT 2 COMMENT '存储层级：2温表 3冷表 4文件',
    `file_path`        VARCHAR(500) DEFAULT NULL COMMENT '文件存储路径（storage_level=4时使用）',
    PRIMARY KEY (`id`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_operation_type` (`operation_type`),
    INDEX `idx_target` (`target_type`, `target_id`),
    INDEX `idx_trace_id` (`trace_id`),
    INDEX `idx_archived_time` (`archived_time`),
    INDEX `idx_archive_batch_id` (`archive_batch_id`),
    INDEX `idx_storage_level` (`storage_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志归档表（3个月以上）';

-- 归档任务记录表
CREATE TABLE IF NOT EXISTS `log_archive_task` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `batch_id`          VARCHAR(64)  NOT NULL COMMENT '归档批次ID',
    `task_type`         TINYINT      NOT NULL COMMENT '任务类型：1热->温 2温->冷 3冷->文件',
    `source_level`      TINYINT      NOT NULL COMMENT '源存储层级：1热表 2温表 3冷表',
    `target_level`      TINYINT      NOT NULL COMMENT '目标存储层级：2温表 3冷表 4文件',
    `start_time`        DATETIME     NOT NULL COMMENT '处理的日志起始时间',
    `end_time`          DATETIME     NOT NULL COMMENT '处理的日志结束时间',
    `total_count`       BIGINT       NOT NULL DEFAULT 0 COMMENT '总记录数',
    `success_count`     BIGINT       NOT NULL DEFAULT 0 COMMENT '成功迁移数',
    `fail_count`        BIGINT       NOT NULL DEFAULT 0 COMMENT '失败数',
    `status`            TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0待执行 1执行中 2成功 3失败',
    `error_msg`         TEXT         DEFAULT NULL COMMENT '错误信息',
    `file_path`         VARCHAR(500) DEFAULT NULL COMMENT '文件路径（冷->文件时）',
    `file_checksum`     VARCHAR(64)  DEFAULT NULL COMMENT '文件校验值',
    `file_size`         BIGINT       DEFAULT NULL COMMENT '文件大小（字节）',
    `operator_id`       BIGINT       DEFAULT NULL COMMENT '操作人ID（手动触发时）',
    `operator_name`     VARCHAR(100) DEFAULT NULL COMMENT '操作人名称',
    `execute_start_time` DATETIME    DEFAULT NULL COMMENT '执行开始时间',
    `execute_end_time`  DATETIME     DEFAULT NULL COMMENT '执行结束时间',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_batch_id` (`batch_id`),
    INDEX `idx_task_type` (`task_type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_time_range` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='日志归档任务记录表';

-- 日志存储策略配置表
CREATE TABLE IF NOT EXISTS `log_storage_policy` (
    `id`                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `policy_name`           VARCHAR(100) NOT NULL COMMENT '策略名称',
    `hot_days`              INT          NOT NULL DEFAULT 7 COMMENT '热数据保留天数',
    `warm_days`             INT          NOT NULL DEFAULT 90 COMMENT '温数据保留天数（含热数据）',
    `cold_days`             INT          NOT NULL DEFAULT 1095 COMMENT '冷数据保留天数（含热+温，默认3年合规期）',
    `auto_archive_enabled`  TINYINT      NOT NULL DEFAULT 1 COMMENT '是否启用自动归档：1启用 0禁用',
    `archive_cron`          VARCHAR(100) NOT NULL DEFAULT '0 0 2 * * ?' COMMENT '自动归档定时表达式',
    `file_export_enabled`   TINYINT      NOT NULL DEFAULT 0 COMMENT '是否启用文件导出归档：1启用 0禁用',
    `file_storage_path`     VARCHAR(500) DEFAULT NULL COMMENT '文件存储根路径',
    `file_compress_enabled` TINYINT      NOT NULL DEFAULT 1 COMMENT '文件是否压缩：1是 0否',
    `integrity_verify_enabled` TINYINT   NOT NULL DEFAULT 1 COMMENT '归档时完整性校验：1启用 0禁用',
    `delete_after_archive`  TINYINT      NOT NULL DEFAULT 1 COMMENT '归档后是否删除源数据：1是 0否',
    `batch_size`            INT          NOT NULL DEFAULT 1000 COMMENT '每批处理记录数',
    `remark`                VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `create_time`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='日志存储策略配置表';

-- 插入默认存储策略
INSERT IGNORE INTO `log_storage_policy` 
(`policy_name`, `hot_days`, `warm_days`, `cold_days`, `auto_archive_enabled`, `archive_cron`, 
 `file_export_enabled`, `file_storage_path`, `file_compress_enabled`, `integrity_verify_enabled`, 
 `delete_after_archive`, `batch_size`, `remark`)
VALUES 
('默认合规策略', 7, 90, 1095, 1, '0 0 2 * * ?', 
 0, '/data/log-archive', 1, 1, 
 1, 1000, '遵循三级等保合规要求，日志保留3年以上');

CREATE TABLE IF NOT EXISTS `video_category` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `name`        VARCHAR(100) NOT NULL,
    `description` VARCHAR(500) DEFAULT NULL,
    `sort`        INT          NOT NULL DEFAULT 0,
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '0禁用 1启用',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='视频分类表';

CREATE TABLE IF NOT EXISTS `video` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `title`           VARCHAR(200) NOT NULL,
    `description`     TEXT         DEFAULT NULL,
    `category_id`     BIGINT       NOT NULL,
    `cover_url`       VARCHAR(500) DEFAULT NULL,
    `video_url`       VARCHAR(500) NOT NULL,
    `duration`        VARCHAR(20)   DEFAULT NULL,
    `file_size`       BIGINT       DEFAULT NULL,
    `view_count`      INT          NOT NULL DEFAULT 0,
    `like_count`      INT          NOT NULL DEFAULT 0,
    `rating`          DECIMAL(3,1)   DEFAULT 0.0,
    `tags`            VARCHAR(500) DEFAULT NULL,
    `status`          TINYINT      NOT NULL DEFAULT 0 COMMENT '0草稿 1已发布 2已下架',
    `create_by`       BIGINT       DEFAULT NULL,
    `create_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`     DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`         TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_create_by` (`create_by`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='视频表';

INSERT IGNORE INTO `video_category` (`id`, `name`, `description`, `sort`, `status`) VALUES
(1, '考试技巧', '各类考试的备考技巧和方法指导', 1, 1),
(2, '课程讲解', '系统的知识点精讲', 2, 1),
(3, '真题解析', '历年真题详细解析', 3, 1),
(4, '面试指导', '面试技巧和经验分享', 4, 1),
(5, '学习方法', '高效学习方法和心得', 5, 1);

INSERT IGNORE INTO `video` (`id`, `title`, `description`, `category_id`, `cover_url`, `video_url`, `duration`, `view_count`, `like_count`, `rating`, `tags`, `status`, `create_by`) VALUES
(1, '如何高效备考公务员考试', '详细讲解公务员考试的备考计划和方法', 1, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=video%20cover%20exam%20study%20preparation&image_size=landscape_16_9', 'https://www.w3schools.com/html/mov_bbb.mp4', '45:30', 1250, 89, 4.8, '公务员,备考,方法', 1, 1),
(2, '高等数学-极限知识点精讲', '系统讲解高等数学中极限的概念和解题方法', 2, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=video%20cover%20mathematics%20calculus%20education&image_size=landscape_16_9', 'https://www.w3schools.com/html/mov_bbb.mp4', '60:00', 3200, 256, 4.9, '高等数学,极限,教学', 1, 1),
(3, '2023年考研英语真题解析', '详细解析2023年考研英语真题', 3, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=video%20cover%20english%20exam%20study&image_size=landscape_16_9', 'https://www.w3schools.com/html/mov_bbb.mp4', '90:00', 5600, 423, 4.7, '考研,英语,真题解析', 1, 1),
(4, '面试常见问题及回答技巧', '分享面试中常见问题的回答技巧和注意事项', 4, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=video%20cover%20interview%20job%20career&image_size=landscape_16_9', 'https://www.w3schools.com/html/mov_bbb.mp4', '35:00', 8900, 678, 4.9, '面试,技巧,求职', 1, 1),
(5, '如何制定高效的学习方法', '分享几种高效的学习方法，助你提升学习效率', 5, 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=video%20cover%20learning%20study%20education&image_size=landscape_16_9', 'https://www.w3schools.com/html/mov_bbb.mp4', '28:00', 4500, 312, 4.6, '学习方法,效率,技巧', 1, 1);
