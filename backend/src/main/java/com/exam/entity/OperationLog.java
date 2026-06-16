package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    private String module;

    private String operation;

    private String method;

    private String params;

    private String ip;

    private Integer status;

    private String errorMsg;

    private Integer operationType;

    private String targetType;

    private String targetId;

    private String beforeState;

    private String afterState;

    private String userAgent;

    private String traceId;

    private String checksum;

    private String previousChecksum;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
