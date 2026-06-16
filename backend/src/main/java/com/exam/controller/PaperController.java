package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.annotation.Log;
import com.exam.common.Result;
import com.exam.dto.PaperDTO;
import com.exam.service.PaperService;
import com.exam.vo.PaperVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/paper")
public class PaperController {

    @Autowired
    private PaperService paperService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "试卷管理", operation = "分页查询试卷列表", operationType = 4, targetType = "paper")
    public Result<IPage<PaperVO>> page(@RequestParam(defaultValue = "1") Integer current,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) Long subjectId,
                                       @RequestParam(required = false) Integer status) {
        return Result.ok(paperService.page(current, size, subjectId, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    @Log(module = "试卷管理", operation = "查询试卷详情", operationType = 4, targetType = "paper")
    public Result<PaperVO> getDetail(@PathVariable Long id) {
        return Result.ok(paperService.getDetail(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "试卷管理", operation = "新增试卷", operationType = 1, targetType = "paper", recordState = true)
    public Result<Boolean> save(@RequestBody @Valid PaperDTO dto) {
        return Result.ok(paperService.save(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "试卷管理", operation = "编辑试卷", operationType = 2, targetType = "paper", recordState = true)
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Valid PaperDTO dto) {
        return Result.ok(paperService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "试卷管理", operation = "删除试卷", operationType = 3, targetType = "paper", recordState = true)
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.ok(paperService.removeById(id));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "试卷管理", operation = "发布试卷", operationType = 2, targetType = "paper", recordState = true)
    public Result<Boolean> publish(@PathVariable Long id) {
        return Result.ok(paperService.publish(id));
    }
}
