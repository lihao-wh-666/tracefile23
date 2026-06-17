SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE exam_db;
SET NAMES utf8mb4;

CREATE TABLE `video_category` (
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

CREATE TABLE `video` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `title`           VARCHAR(200) NOT NULL,
    `description`     TEXT         DEFAULT NULL,
    `category_id`     BIGINT       NOT NULL,
    `cover_url`     VARCHAR(500) DEFAULT NULL,
    `video_url`     VARCHAR(500) NOT NULL,
    `duration`      VARCHAR(20)   DEFAULT NULL,
    `file_size`     BIGINT       DEFAULT NULL,
    `view_count`    INT          NOT NULL DEFAULT 0,
    `like_count`    INT          NOT NULL DEFAULT 0,
    `rating`        DECIMAL(3,1)   DEFAULT 0.0,
    `tags`          VARCHAR(500) DEFAULT NULL,
    `status`        TINYINT      NOT NULL DEFAULT 0 COMMENT '0草稿 1已发布 2已下架',
    `create_by`     BIGINT       DEFAULT NULL,
    `create_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    `update_time`   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted`       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_create_by` (`create_by`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='视频表';

INSERT INTO `video_category` (`name`, `description`, `sort`, `status`) VALUES
('考试技巧', '各类考试的备考技巧和方法指导', 1, 1),
('课程讲解', '系统的知识点精讲', 2, 1),
('真题解析', '历年真题详细解析', 3, 1),
('面试指导', '面试技巧和经验分享', 4, 1),
('学习方法', '高效学习方法和心得', 5, 1);

INSERT INTO `video` (`title`, `description`, `category_id`, `cover_url`, `video_url`, `duration`, `view_count`, `like_count`, `rating`, `tags`, `status`, `create_by`) VALUES
('如何高效备考公务员考试', '详细讲解公务员考试的备考计划和方法', 1, '/uploads/video/cover1.jpg', '/uploads/video1.mp4', '45:30', 1250, 89, 4.8, '公务员,备考,方法', 1, 1),
('高等数学-极限知识点精讲', '系统讲解高等数学中极限的概念和解题方法', 2, '/uploads/cover2.jpg', '/uploads/video2.mp4', '60:00', 3200, 256, 4.9, '高等数学,极限,教学', 1, 1),
('2023年考研英语真题解析', '详细解析2023年考研英语真题', 3, '/uploads/cover3.jpg', '/uploads/video3.mp4', '90:00', 5600, 423, 4.7, '考研,英语,真题解析', 1, 1),
('面试常见问题及回答技巧', '分享面试中常见问题的回答技巧和注意事项', 4, '/uploads/cover4.jpg', '/uploads/video4.mp4', '35:00', 8900, 678, 4.9, '面试,技巧,求职', 1, 1),
('如何制定高效的学习方法', '分享几种高效的学习方法，助你提升学习效率', 5, '/uploads/cover5.jpg', '/uploads/video5.mp4', '28:00', 4500, 312, 4.6, '学习方法,效率,技巧', 1, 1);
