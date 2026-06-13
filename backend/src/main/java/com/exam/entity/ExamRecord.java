package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("exam_record")
public class ExamRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long examId;

    private Long userId;

    private Long paperId;

    private LocalDateTime startTime;

    private LocalDateTime submitTime;

    private Integer score;

    private Integer status;

    private Integer duration;

    private Integer pauseCount;

    private Integer totalPauseTime;

    private String questionOrder;

    private LocalDateTime lastPauseTime;

    private LocalDateTime lastResumeTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
