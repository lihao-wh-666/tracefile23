USE exam_db;

ALTER TABLE `user` ADD COLUMN `login_locked` TINYINT NOT NULL DEFAULT 0 COMMENT '登录锁定状态：0未锁定 1已锁定' AFTER `status`;
ALTER TABLE `user` ADD COLUMN `lock_end_time` DATETIME DEFAULT NULL COMMENT '锁定到期时间' AFTER `login_locked`;
