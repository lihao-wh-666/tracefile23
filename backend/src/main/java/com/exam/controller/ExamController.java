package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.common.Result;
import com.exam.dto.ExamDTO;
import com.exam.service.ExamService;
import com.exam.vo.ExamVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/exam")
public class ExamController {

    @Autowired
    private ExamService examService;

    @GetMapping("/page")
    public Result<IPage<ExamVO>> page(@RequestParam(defaultValue = "1") Integer current,
                                      @RequestParam(defaultValue = "10") Integer size,
                                      @RequestParam(required = false) Integer status) {
        return Result.ok(examService.page(current, size, status));
    }

    @GetMapping("/{id}")
    public Result<ExamVO> getDetail(@PathVariable Long id) {
        return Result.ok(examService.getDetail(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody @Valid ExamDTO dto) {
        return Result.ok(examService.save(dto));
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Valid ExamDTO dto) {
        return Result.ok(examService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.ok(examService.removeById(id));
    }
}
