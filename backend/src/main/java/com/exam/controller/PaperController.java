package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.common.Result;
import com.exam.dto.PaperDTO;
import com.exam.service.PaperService;
import com.exam.vo.PaperVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/paper")
public class PaperController {

    @Autowired
    private PaperService paperService;

    @GetMapping("/page")
    public Result<IPage<PaperVO>> page(@RequestParam(defaultValue = "1") Integer current,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) Long subjectId,
                                       @RequestParam(required = false) Integer status) {
        return Result.ok(paperService.page(current, size, subjectId, status));
    }

    @GetMapping("/{id}")
    public Result<PaperVO> getDetail(@PathVariable Long id) {
        return Result.ok(paperService.getDetail(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody @Valid PaperDTO dto) {
        return Result.ok(paperService.save(dto));
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody @Valid PaperDTO dto) {
        return Result.ok(paperService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.ok(paperService.removeById(id));
    }

    @PutMapping("/{id}/publish")
    public Result<Boolean> publish(@PathVariable Long id) {
        return Result.ok(paperService.publish(id));
    }
}
