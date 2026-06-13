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
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`     BIGINT       DEFAULT NULL,
    `username`    VARCHAR(50)  DEFAULT NULL,
    `module`      VARCHAR(100) DEFAULT NULL,
    `operation`   VARCHAR(200) DEFAULT NULL,
    `method`      VARCHAR(200) DEFAULT NULL,
    `params`      TEXT         DEFAULT NULL,
    `ip`          VARCHAR(50)  DEFAULT NULL,
    `status`      TINYINT      NOT NULL DEFAULT 1,
    `error_msg`   VARCHAR(500) DEFAULT NULL,
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';

INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 1, 1);

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
('login.timeout.minutes', '30', '登录超时时间(分钟)', '用户无操作超过该时间将自动登出，单位：分钟', 2);
