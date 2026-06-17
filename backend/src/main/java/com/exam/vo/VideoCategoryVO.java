package com.exam.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoCategoryVO {

    private Long id;

    private String name;

    private String description;

    private Integer sort;

    private Integer status;

    private String statusName;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
