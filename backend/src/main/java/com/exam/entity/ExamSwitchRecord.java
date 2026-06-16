package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exam_switch_record")
public class ExamSwitchRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long recordId;

    private Long userId;

    private Long examId;

    private Integer switchType;

    private LocalDateTime switchTime;

    private Integer duration;

    private String appName;

    private Integer screenshotDetected;

    private Integer screenRecordDetected;

    private String details;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
