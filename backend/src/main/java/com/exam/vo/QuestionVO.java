package com.exam.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuestionVO {

    private Long id;

    private Long subjectId;

    private String subjectName;

    private Integer type;

    private String content;

    private String optionA;

    private String optionB;

    private String optionC;

    private String optionD;

    private String answer;

    private String analysis;

    private Integer score;

    private Integer difficulty;

    private Long createBy;

    private LocalDateTime createTime;
}
