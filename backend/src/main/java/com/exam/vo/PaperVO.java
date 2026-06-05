package com.exam.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PaperVO {

    private Long id;

    private String name;

    private Long subjectId;

    private String subjectName;

    private Integer totalScore;

    private Integer passScore;

    private Integer duration;

    private Integer status;

    private Long createBy;

    private LocalDateTime createTime;

    private List<QuestionVO> questions;
}
