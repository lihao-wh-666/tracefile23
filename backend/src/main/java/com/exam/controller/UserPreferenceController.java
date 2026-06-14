package com.exam.controller;

import com.exam.annotation.Log;
import com.exam.common.Result;
import com.exam.dto.UserPreferenceDTO;
import com.exam.entity.UserPreference;
import com.exam.service.UserPreferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/user-preference")
public class UserPreferenceController {

    @Autowired
    private UserPreferenceService userPreferenceService;

    @GetMapping
    public Result<UserPreference> getPreference() {
        return Result.ok(userPreferenceService.getCurrentUserPreference());
    }

    @PutMapping
    @Log(module = "个人偏好设置", operation = "更新偏好设置")
    public Result<Boolean> savePreference(@RequestBody @Valid UserPreferenceDTO dto) {
        return Result.ok(userPreferenceService.savePreference(dto));
    }
}
