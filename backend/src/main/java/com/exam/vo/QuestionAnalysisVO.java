package com.exam.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class QuestionAnalysisVO {

    private Long questionId;

    private String content;

    private Integer type;

    private String typeName;

    private Integer difficulty;

    private String difficultyName;

    private Integer score;

    private Long subjectId;

    private String subjectName;

    private Integer totalAnswerCount;

    private Integer correctCount;

    private Integer wrongCount;

    private BigDecimal correctRate;

    private BigDecimal wrongRate;

    private BigDecimal averageScore;

    private Integer totalScoreSum;

    private Integer answerUserCount;
}
