package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.annotation.Log;
import com.exam.common.Result;
import com.exam.dto.ExamDTO;
import com.exam.service.ExamService;
import com.exam.vo.ExamVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/exam")
public class ExamController {

    @Autowired
    private ExamService examService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    @Log(module = "考试管理", operation = "分页查询考试列表", operationType = 4, targetType = "exam")
    public Result<IPage<ExamVO>> page(@RequestParam(defaultValue = "1") Integer current,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestParam(required = false) Integer status) {
        return Result.ok(examService.page(current, size, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    @Log(module = "考试管理", operation = "查询考试详情", operationType = 4, targetType = "exam")
    public Result<ExamVO> getDetail(@PathVariable Long id) {
        return Result.ok(examService.getDetail(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "考试管理", operation = "新增考试", operationType = 1, targetType = "exam", recordState = true)
    public Result<Boolean> save(@RequestBody @Valid ExamDTO dto) {
        return Result.ok(examService.save(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "考试管理", operation = "编辑考试", operationType = 2, targetType = "exam", recordState = true)
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Valid ExamDTO dto) {
        return Result.ok(examService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "考试管理", operation = "删除考试", operationType = 3, targetType = "exam", recordState = true)
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.ok(examService.removeById(id));
    }
}
