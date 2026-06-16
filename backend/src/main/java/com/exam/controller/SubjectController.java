package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.annotation.Log;
import com.exam.common.Result;
import com.exam.entity.Subject;
import com.exam.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/subject")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "科目管理", operation = "分页查询科目列表", operationType = 4, targetType = "subject")
    public Result<IPage<Subject>> page(@RequestParam(defaultValue = "1") Integer current,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String name) {
        return Result.ok(subjectService.page(current, size, name));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    @Log(module = "科目管理", operation = "查询科目列表", operationType = 4, targetType = "subject")
    public Result<List<Subject>> list() {
        return Result.ok(subjectService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "科目管理", operation = "查询科目详情", operationType = 4, targetType = "subject")
    public Result<Subject> getById(@PathVariable Long id) {
        return Result.ok(subjectService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "科目管理", operation = "新增科目", operationType = 1, targetType = "subject", recordState = true)
    public Result<Boolean> save(@RequestBody @Valid Subject subject) {
        return Result.ok(subjectService.save(subject));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "科目管理", operation = "编辑科目", operationType = 2, targetType = "subject", recordState = true)
    public Result<Boolean> update(@RequestBody @Valid Subject subject) {
        return Result.ok(subjectService.updateById(subject));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "科目管理", operation = "删除科目", operationType = 3, targetType = "subject", recordState = true)
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.ok(subjectService.removeById(id));
    }
}
