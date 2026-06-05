package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.common.Result;
import com.exam.entity.Subject;
import com.exam.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/subject")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    @GetMapping("/page")
    public Result<IPage<Subject>> page(@RequestParam(defaultValue = "1") Integer current,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) String name) {
        return Result.ok(subjectService.page(current, size, name));
    }

    @GetMapping("/list")
    public Result<List<Subject>> list() {
        return Result.ok(subjectService.list());
    }

    @GetMapping("/{id}")
    public Result<Subject> getById(@PathVariable Long id) {
        return Result.ok(subjectService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody @Valid Subject subject) {
        return Result.ok(subjectService.save(subject));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody @Valid Subject subject) {
        return Result.ok(subjectService.updateById(subject));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.ok(subjectService.removeById(id));
    }
}
