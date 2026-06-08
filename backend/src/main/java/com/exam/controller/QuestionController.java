package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.common.Result;
import com.exam.dto.QuestionDTO;
import com.exam.service.QuestionService;
import com.exam.vo.QuestionImportVO;
import com.exam.vo.QuestionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('1', '2')")
    public Result<IPage<QuestionVO>> page(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) Long subjectId,
                                          @RequestParam(required = false) Integer type,
                                          @RequestParam(required = false) Integer difficulty) {
        return Result.ok(questionService.page(current, size, subjectId, type, difficulty));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('1', '2')")
    public Result<QuestionVO> getDetail(@PathVariable Long id) {
        return Result.ok(questionService.getDetail(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('1', '2')")
    public Result<Boolean> save(@RequestBody @Valid QuestionDTO dto) {
        return Result.ok(questionService.save(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('1', '2')")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Valid QuestionDTO dto) {
        return Result.ok(questionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('1', '2')")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.ok(questionService.removeById(id));
    }

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('1', '2')")
    public Result<QuestionImportVO> importQuestions(@RequestParam("file") MultipartFile file,
                                                    @RequestParam(required = false) Long subjectId) {
        return Result.ok(questionService.importQuestions(file, subjectId));
    }

    @GetMapping("/template")
    @PreAuthorize("hasAnyRole('1', '2')")
    public void downloadTemplate(HttpServletResponse response) {
        questionService.downloadTemplate(response);
    }
}
