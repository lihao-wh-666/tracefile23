package com.exam.service;

import com.exam.dto.QuestionAnalysisDTO;
import com.exam.vo.QuestionAnalysisReportVO;
import com.exam.vo.QuestionAnalysisVO;

import java.util.List;

public interface QuestionAnalysisService {

    QuestionAnalysisReportVO generateAnalysisReport(QuestionAnalysisDTO dto);

    List<QuestionAnalysisVO> getQuestionAnalysisList(QuestionAnalysisDTO dto);
}
