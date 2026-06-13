package com.exam.dto;

import lombok.Data;

@Data
public class QuestionAnalysisDTO {

    private Long subjectId;

    private Long paperId;

    private BigDecimal wrongRateThreshold;

    private Integer topN;
}
