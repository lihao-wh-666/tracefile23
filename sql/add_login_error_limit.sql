USE exam_db;

INSERT IGNORE INTO `system_config` (`config_key`, `config_value`, `config_name`, `description`, `value_type`) VALUES
('login.max.error.count', '5', '登录最大错误次数', '密码错误次数达到该值后锁定账号，0表示不限制', 2),
('login.lock.duration.minutes', '30', '账号锁定时长(分钟)', '密码错误次数超限后账号锁定的时长，单位：分钟', 2);
