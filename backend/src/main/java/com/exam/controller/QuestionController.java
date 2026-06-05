package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.common.Result;
import com.exam.dto.QuestionDTO;
import com.exam.service.QuestionService;
import com.exam.vo.QuestionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/page")
    public Result<IPage<QuestionVO>> page(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) Long subjectId,
                                          @RequestParam(required = false) Integer type,
                                          @RequestParam(required = false) Integer difficulty) {
        return Result.ok(questionService.page(current, size, subjectId, type, difficulty));
    }

    @GetMapping("/{id}")
    public Result<QuestionVO> getDetail(@PathVariable Long id) {
        return Result.ok(questionService.getDetail(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody @Valid QuestionDTO dto) {
        return Result.ok(questionService.save(dto));
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Valid QuestionDTO dto) {
        return Result.ok(questionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.ok(questionService.removeById(id));
    }
}
