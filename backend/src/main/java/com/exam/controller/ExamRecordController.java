package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.common.Result;
import com.exam.dto.SubmitExamDTO;
import com.exam.entity.ExamAnswer;
import com.exam.mapper.ExamAnswerMapper;
import com.exam.service.ExamRecordService;
import com.exam.vo.ExamRecordVO;
import com.exam.vo.PersonalScoreStatVO;
import com.exam.vo.ScoreStatVO;
import com.exam.vo.WrongQuestionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/record")
public class ExamRecordController {

    @Autowired
    private ExamRecordService recordService;

    @Autowired
    private ExamAnswerMapper examAnswerMapper;

    @GetMapping("/page")
    public Result<IPage<ExamRecordVO>> page(@RequestParam(defaultValue = "1") Integer current,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) Long examId,
                                            @RequestParam(required = false) Long userId,
                                            @RequestParam(required = false) Integer status) {
        return Result.ok(recordService.page(current, size, examId, userId, status));
    }

    @GetMapping("/{id}")
    public Result<ExamRecordVO> getDetail(@PathVariable Long id) {
        return Result.ok(recordService.getDetail(id));
    }

    @GetMapping("/{id}/answers")
    public Result<List<ExamAnswer>> getRecordAnswers(@PathVariable Long id) {
        List<ExamAnswer> answers = examAnswerMapper.selectList(
                new LambdaQueryWrapper<ExamAnswer>().eq(ExamAnswer::getRecordId, id));
        return Result.ok(answers);
    }

    @PostMapping("/start/{examId}")
    public Result<ExamRecordVO> startExam(@PathVariable Long examId) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        return Result.ok(recordService.startExam(examId, userId));
    }

    @PostMapping("/submit")
    public Result<ExamRecordVO> submitExam(@RequestBody @Valid SubmitExamDTO dto) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        return Result.ok(recordService.submitExam(dto, userId));
    }

    @GetMapping("/stats/{examId}")
    public Result<List<ScoreStatVO>> scoreStats(@PathVariable Long examId) {
        return Result.ok(recordService.scoreStats(examId));
    }

    @GetMapping("/my/stat")
    public Result<PersonalScoreStatVO> getMyStat() {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        return Result.ok(recordService.getPersonalStat(userId));
    }

    @GetMapping("/my/list")
    public Result<IPage<ExamRecordVO>> getMyList(@RequestParam(defaultValue = "1") Integer current,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        return Result.ok(recordService.getMyRecords(current, size, userId));
    }

    @GetMapping("/my/wrong-questions")
    public Result<IPage<WrongQuestionVO>> getMyWrongQuestions(@RequestParam(defaultValue = "1") Integer current,
                                                              @RequestParam(defaultValue = "10") Integer size) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        return Result.ok(recordService.getWrongQuestions(current, size, userId));
    }
}
