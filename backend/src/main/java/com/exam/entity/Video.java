package com.exam.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("video")
public class Video {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private Long categoryId;

    @TableField(exist = false)
    private String categoryName;

    private String coverUrl;

    private String videoUrl;

    private String duration;

    private Long fileSize;

    private Integer viewCount;

    private Integer likeCount;

    private BigDecimal rating;

    private String tags;

    private Integer status;

    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
