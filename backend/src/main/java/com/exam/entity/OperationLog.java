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

    @TableField(select = false)
    private Integer operationType;

    @TableField(select = false)
    private String targetType;

    @TableField(select = false)
    private String targetId;

    @TableField(select = false)
    private String beforeState;

    @TableField(select = false)
    private String afterState;

    @TableField(select = false)
    private String userAgent;

    @TableField(select = false)
    private String traceId;

    @TableField(select = false)
    private String checksum;

    @TableField(select = false)
    private String previousChecksum;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
