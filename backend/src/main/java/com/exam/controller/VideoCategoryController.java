package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.annotation.Log;
import com.exam.common.Result;
import com.exam.entity.VideoCategory;
import com.exam.service.VideoCategoryService;
import com.exam.vo.VideoCategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/video-category")
public class VideoCategoryController {

    @Autowired
    private VideoCategoryService videoCategoryService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "视频分类管理", operation = "分页查询视频分类列表", operationType = 4, targetType = "videoCategory")
    public Result<IPage<VideoCategoryVO>> page(@RequestParam(defaultValue = "1") Integer current,
                                               @RequestParam(defaultValue = "10") Integer size,
                                               @RequestParam(required = false) String name) {
        return Result.ok(videoCategoryService.page(current, size, name));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('1', '2', '3')")
    @Log(module = "视频分类管理", operation = "查询视频分类列表", operationType = 4, targetType = "videoCategory")
    public Result<List<VideoCategoryVO>> list() {
        return Result.ok(videoCategoryService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "视频分类管理", operation = "查询视频分类详情", operationType = 4, targetType = "videoCategory")
    public Result<VideoCategoryVO> getById(@PathVariable Long id) {
        return Result.ok(videoCategoryService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "视频分类管理", operation = "新增视频分类", operationType = 1, targetType = "videoCategory", recordState = true)
    public Result<Boolean> save(@RequestBody @Valid VideoCategory videoCategory) {
        return Result.ok(videoCategoryService.save(videoCategory));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "视频分类管理", operation = "编辑视频分类", operationType = 2, targetType = "videoCategory", recordState = true)
    public Result<Boolean> update(@RequestBody @Valid VideoCategory videoCategory) {
        return Result.ok(videoCategoryService.updateById(videoCategory));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('1', '2')")
    @Log(module = "视频分类管理", operation = "删除视频分类", operationType = 3, targetType = "videoCategory", recordState = true)
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.ok(videoCategoryService.removeById(id));
    }
}
