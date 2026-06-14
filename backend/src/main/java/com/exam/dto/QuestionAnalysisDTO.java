package com.exam.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuestionAnalysisDTO {

    private Long subjectId;

    private Long paperId;

    private BigDecimal wrongRateThreshold;

    private Integer topN;
}
