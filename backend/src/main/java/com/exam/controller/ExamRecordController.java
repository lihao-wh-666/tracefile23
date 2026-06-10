package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.common.Result;
import com.exam.dto.PauseExamDTO;
import com.exam.dto.SaveAnswerDTO;
import com.exam.dto.SaveAnswersDTO;
import com.exam.dto.SubmitExamDTO;
import com.exam.entity.ExamAnswer;
import com.exam.service.ExamRecordService;
import com.exam.vo.ExamRecordVO;
import com.exam.vo.PersonalScoreStatVO;
import com.exam.vo.ScoreStatVO;
import com.exam.vo.WrongQuestionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/record")
public class ExamRecordController {

    @Autowired
    private ExamRecordService recordService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('1', '2')")
    public Result<IPage<ExamRecordVO>> page(@RequestParam(defaultValue = "1") Integer current,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) Long examId,
                                            @RequestParam(required = false) Long userId,
                                            @RequestParam(required = false) Integer status) {
        return Result.ok(recordService.page(current, size, examId, userId, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    public Result<ExamRecordVO> getDetail(@PathVariable Long id) {
        return Result.ok(recordService.getDetail(id));
    }

    @GetMapping("/{id}/answers")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    public Result<List<ExamAnswer>> getRecordAnswers(@PathVariable Long id) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        return Result.ok(recordService.getRecordAnswers(id, userId));
    }

    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    public Result<ExamRecordVO> getCurrentExam() {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        return Result.ok(recordService.getCurrentExam(userId));
    }

    @PostMapping("/start/{examId}")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    public Result<ExamRecordVO> startExam(@PathVariable Long examId) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        return Result.ok(recordService.startExam(examId, userId));
    }

    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    public Result<ExamRecordVO> submitExam(@RequestBody @Valid SubmitExamDTO dto) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        return Result.ok(recordService.submitExam(dto, userId));
    }

    @PostMapping("/save-answer")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    public Result<Boolean> saveAnswer(@RequestBody @Valid SaveAnswerDTO dto) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        return Result.ok(recordService.saveAnswer(dto, userId));
    }

    @PostMapping("/save-answers")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    public Result<Boolean> saveAnswers(@RequestBody @Valid SaveAnswersDTO dto) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        return Result.ok(recordService.saveAnswers(dto, userId));
    }

    @PostMapping("/pause")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    public Result<ExamRecordVO> pauseExam(@RequestBody @Valid PauseExamDTO dto) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        return Result.ok(recordService.pauseExam(dto, userId));
    }

    @PostMapping("/resume")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    public Result<ExamRecordVO> resumeExam(@RequestBody @Valid PauseExamDTO dto) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        return Result.ok(recordService.resumeExam(dto, userId));
    }

    @GetMapping("/stats/{examId}")
    @PreAuthorize("hasAnyRole('1', '2')")
    public Result<List<ScoreStatVO>> scoreStats(@PathVariable Long examId) {
        return Result.ok(recordService.scoreStats(examId));
    }

    @GetMapping("/my/stat")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    public Result<PersonalScoreStatVO> getMyStat() {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        return Result.ok(recordService.getPersonalStat(userId));
    }

    @GetMapping("/my/list")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    public Result<IPage<ExamRecordVO>> getMyList(@RequestParam(defaultValue = "1") Integer current,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        return Result.ok(recordService.getMyRecords(current, size, userId));
    }

    @GetMapping("/my/wrong-questions")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    public Result<IPage<WrongQuestionVO>> getMyWrongQuestions(@RequestParam(defaultValue = "1") Integer current,
                                                              @RequestParam(defaultValue = "10") Integer size) {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        return Result.ok(recordService.getWrongQuestions(current, size, userId));
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasAnyRole('1', '2')")
    public void exportExcel(@RequestParam(required = false) Long examId, HttpServletResponse response) throws Exception {
        byte[] data = recordService.exportExcel(examId);
        String fileName = "成绩统计_" + System.currentTimeMillis() + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
        response.setContentLength(data.length);
        OutputStream out = response.getOutputStream();
        out.write(data);
        out.flush();
        out.close();
    }

    @GetMapping("/export/csv")
    @PreAuthorize("hasAnyRole('1', '2')")
    public void exportCsv(@RequestParam(required = false) Long examId, HttpServletResponse response) throws Exception {
        byte[] data = recordService.exportCsv(examId);
        String fileName = "成绩统计_" + System.currentTimeMillis() + ".csv";
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
        response.setContentLength(data.length);
        OutputStream out = response.getOutputStream();
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        out.write(data);
        out.flush();
        out.close();
    }

    @GetMapping("/export/pdf")
    @PreAuthorize("hasAnyRole('1', '2')")
    public void exportPdf(@RequestParam(required = false) Long examId, HttpServletResponse response) throws Exception {
        byte[] data = recordService.exportPdf(examId);
        String fileName = "成绩统计_" + System.currentTimeMillis() + ".pdf";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
        response.setContentLength(data.length);
        OutputStream out = response.getOutputStream();
        out.write(data);
        out.flush();
        out.close();
    }

    @GetMapping("/my/export/records/excel")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    public void exportMyRecordsExcel(HttpServletResponse response) throws Exception {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        byte[] data = recordService.exportMyRecordsExcel(userId);
        String fileName = "我的考试成绩_" + System.currentTimeMillis() + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
        response.setContentLength(data.length);
        OutputStream out = response.getOutputStream();
        out.write(data);
        out.flush();
        out.close();
    }

    @GetMapping("/my/export/records/csv")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    public void exportMyRecordsCsv(HttpServletResponse response) throws Exception {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        byte[] data = recordService.exportMyRecordsCsv(userId);
        String fileName = "我的考试成绩_" + System.currentTimeMillis() + ".csv";
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
        response.setContentLength(data.length);
        OutputStream out = response.getOutputStream();
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        out.write(data);
        out.flush();
        out.close();
    }

    @GetMapping("/my/export/wrong-questions/excel")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    public void exportMyWrongQuestionsExcel(HttpServletResponse response) throws Exception {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        byte[] data = recordService.exportMyWrongQuestionsExcel(userId);
        String fileName = "我的错题明细_" + System.currentTimeMillis() + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
        response.setContentLength(data.length);
        OutputStream out = response.getOutputStream();
        out.write(data);
        out.flush();
        out.close();
    }

    @GetMapping("/my/export/wrong-questions/csv")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    public void exportMyWrongQuestionsCsv(HttpServletResponse response) throws Exception {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        byte[] data = recordService.exportMyWrongQuestionsCsv(userId);
        String fileName = "我的错题明细_" + System.currentTimeMillis() + ".csv";
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
        response.setContentLength(data.length);
        OutputStream out = response.getOutputStream();
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        out.write(data);
        out.flush();
        out.close();
    }
}
