package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.common.Result;
import com.exam.entity.SystemConfig;
import com.exam.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/system-config")
public class SystemConfigController {

    @Autowired
    private SystemConfigService systemConfigService;

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('1')")
    public Result<IPage<SystemConfig>> page(@RequestParam(defaultValue = "1") Integer current,
                                            @RequestParam(defaultValue = "10") Integer size,
                                            @RequestParam(required = false) String configKey,
                                            @RequestParam(required = false) String configName) {
        return Result.ok(systemConfigService.page(current, size, configKey, configName));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('1')")
    public Result<List<SystemConfig>> list() {
        return Result.ok(systemConfigService.list());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('1')")
    public Result<SystemConfig> getById(@PathVariable Long id) {
        return Result.ok(systemConfigService.getById(id));
    }

    @GetMapping("/key/{configKey}")
    @PreAuthorize("hasAnyRole('1')")
    public Result<SystemConfig> getByKey(@PathVariable String configKey) {
        return Result.ok(systemConfigService.getByKey(configKey));
    }

    @GetMapping("/value/{configKey}")
    public Result<String> getValueByKey(@PathVariable String configKey) {
        return Result.ok(systemConfigService.getValueByKey(configKey));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('1')")
    public Result<Boolean> save(@RequestBody @Valid SystemConfig systemConfig) {
        return Result.ok(systemConfigService.save(systemConfig));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('1')")
    public Result<Boolean> update(@RequestBody @Valid SystemConfig systemConfig) {
        return Result.ok(systemConfigService.updateById(systemConfig));
    }

    @PutMapping("/update-by-key")
    @PreAuthorize("hasAnyRole('1')")
    public Result<Boolean> updateByKey(@RequestParam String configKey,
                                       @RequestParam String configValue) {
        return Result.ok(systemConfigService.updateByKey(configKey, configValue));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('1')")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.ok(systemConfigService.removeById(id));
    }

    @PostMapping("/refresh-cache")
    @PreAuthorize("hasAnyRole('1')")
    public Result<Boolean> refreshCache() {
        systemConfigService.refreshCache();
        return Result.ok(true);
    }
}
