package com.exam.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class VideoVO {

    private Long id;

    private String title;

    private String description;

    private Long categoryId;

    private String categoryName;

    private String coverUrl;

    private String videoUrl;

    private String duration;

    private Long fileSize;

    private Integer viewCount;

    private Integer likeCount;

    private BigDecimal rating;

    private String tags;

    private List<String> tagList;

    private Integer status;

    private String statusName;

    private Long createBy;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private String publishDate;
}
