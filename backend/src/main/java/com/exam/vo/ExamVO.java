package com.exam.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamVO {

    private Long id;

    private Long paperId;

    private String paperName;

    private String name;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    private Long createBy;

    private LocalDateTime createTime;
}
