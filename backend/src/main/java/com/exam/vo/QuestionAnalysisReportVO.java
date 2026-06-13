package com.exam.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class QuestionAnalysisReportVO {

    private Long subjectId;

    private String subjectName;

    private Long paperId;

    private String paperName;

    private Integer totalQuestionCount;

    private Integer totalAnswerCount;

    private Integer totalUserCount;

    private BigDecimal overallCorrectRate;

    private BigDecimal overallAverageScore;

    private List<QuestionAnalysisVO> questionAnalysisList;

    private List<HighFrequencyWrongQuestionVO> highFrequencyWrongQuestions;

    private Map<String, List<QuestionAnalysisVO>> analysisByType;

    private Map<String, List<QuestionAnalysisVO>> analysisByDifficulty;

    private List<String> optimizationSuggestions;

    private String generateTime;
}
