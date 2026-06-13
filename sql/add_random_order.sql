USE exam_db;

ALTER TABLE `exam_record` ADD COLUMN `question_order` VARCHAR(1000) DEFAULT NULL COMMENT '题目随机顺序(逗号分隔的question_id)' AFTER `total_pause_time`;

ALTER TABLE `exam_answer` ADD COLUMN `option_order` VARCHAR(20) DEFAULT NULL COMMENT '选项随机顺序(如BCAD)' AFTER `answer`;
