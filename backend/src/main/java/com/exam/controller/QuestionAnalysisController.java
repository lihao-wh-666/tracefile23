package com.exam.controller;

import com.exam.common.Result;
import com.exam.dto.QuestionAnalysisDTO;
import com.exam.service.QuestionAnalysisService;
import com.exam.vo.QuestionAnalysisReportVO;
import com.exam.vo.QuestionAnalysisVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question-analysis")
public class QuestionAnalysisController {

    @Autowired
    private QuestionAnalysisService questionAnalysisService;

    @PostMapping("/report")
    @PreAuthorize("hasAnyRole('1', '2')")
    public Result<QuestionAnalysisReportVO> generateReport(@RequestBody QuestionAnalysisDTO dto) {
        return Result.ok(questionAnalysisService.generateAnalysisReport(dto));
    }

    @PostMapping("/list")
    @PreAuthorize("hasAnyRole('1', '2')")
    public Result<List<QuestionAnalysisVO>> getAnalysisList(@RequestBody QuestionAnalysisDTO dto) {
        return Result.ok(questionAnalysisService.getQuestionAnalysisList(dto));
    }
}
