package com.exam.vo;

import lombok.Data;

@Data
public class ScoreStatVO {

    private Long examId;

    private String examName;

    private Integer totalCount;

    private Double avgScore;

    private Integer maxScore;

    private Integer minScore;

    private Integer passCount;

    private Double passRate;
}
