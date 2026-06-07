package com.exam.vo;

import lombok.Data;

@Data
public class PersonalScoreStatVO {

    private Integer totalExamCount;

    private Integer submittedCount;

    private Integer maxScore;

    private Long maxScoreExamId;

    private String maxScoreExamName;

    private Double avgScore;

    private Double accuracyRate;

    private Integer totalQuestionCount;

    private Integer correctQuestionCount;

    private Integer wrongQuestionCount;

    private Integer totalScore;

    private Integer totalFullScore;
}
