package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.annotation.Log;
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
    @Log(module = "题库管理", operation = "分页查询题目列表", operationType = 4, targetType = "question")
    public Result<IPage<QuestionVO>> page(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) Long subjectId,
                                          @RequestParam(required = false) Integer type,
                                          @RequestParam(required = false) Integer difficulty) {
        return Result.ok(questionService.page(current, size, subjectId, type, difficulty));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "题库管理", operation = "查询题目详情", operationType = 4, targetType = "question")
    public Result<QuestionVO> getDetail(@PathVariable Long id) {
        return Result.ok(questionService.getDetail(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "题库管理", operation = "新增题目", operationType = 1, targetType = "question", recordState = true)
    public Result<Boolean> save(@RequestBody @Valid QuestionDTO dto) {
        return Result.ok(questionService.save(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "题库管理", operation = "编辑题目", operationType = 2, targetType = "question", recordState = true)
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Valid QuestionDTO dto) {
        return Result.ok(questionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "题库管理", operation = "删除题目", operationType = 3, targetType = "question", recordState = true)
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.ok(questionService.removeById(id));
    }

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "题库管理", operation = "批量导入题目", operationType = 8, targetType = "question")
    public Result<QuestionImportVO> importQuestions(@RequestParam("file") MultipartFile file,
                                                    @RequestParam(required = false) Long subjectId) {
        return Result.ok(questionService.importQuestions(file, subjectId));
    }

    @GetMapping("/template")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "题库管理", operation = "下载题目导入模板", operationType = 7, targetType = "question")
    public void downloadTemplate(HttpServletResponse response) {
        questionService.downloadTemplate(response);
    }
}
