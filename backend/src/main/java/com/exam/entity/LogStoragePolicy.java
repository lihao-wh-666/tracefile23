package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("log_storage_policy")
public class LogStoragePolicy {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String policyName;

    private Integer hotDays;

    private Integer warmDays;

    private Integer coldDays;

    private Integer autoArchiveEnabled;

    private String archiveCron;

    private Integer fileExportEnabled;

    private String fileStoragePath;

    private Integer fileCompressEnabled;

    private Integer integrityVerifyEnabled;

    private Integer deleteAfterArchive;

    private Integer batchSize;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
