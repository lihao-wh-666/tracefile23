package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("log_archive_task")
public class LogArchiveTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String batchId;

    private Integer taskType;

    private Integer sourceLevel;

    private Integer targetLevel;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long totalCount;

    private Long successCount;

    private Long failCount;

    private Integer status;

    private String errorMsg;

    private String filePath;

    private String fileChecksum;

    private Long fileSize;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime executeStartTime;

    private LocalDateTime executeEndTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
