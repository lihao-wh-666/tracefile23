package com.exam.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class HighFrequencyWrongQuestionVO {

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

    private Integer wrongCount;

    private BigDecimal wrongRate;

    private Integer wrongRank;

    private String wrongReason;
}
