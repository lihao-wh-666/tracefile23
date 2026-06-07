package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.annotation.Log;
import com.exam.common.Constants;
import com.exam.common.Result;
import com.exam.dto.UserDTO;
import com.exam.entity.User;
import com.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/info")
    public Result<User> info() {
        Long userId = Long.parseLong(org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName());
        return Result.ok(userService.getById(userId));
    }

    @GetMapping("/page")
    @PreAuthorize("hasRole('1')")
    @Log(module = "用户管理", operation = "分页查询用户列表")
    public Result<IPage<User>> page(@RequestParam(defaultValue = "1") Integer current,
                                    @RequestParam(defaultValue = "10") Integer size,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) Integer role,
                                    @RequestParam(required = false) Integer status) {
        return Result.ok(userService.page(current, size, keyword, role, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('1')")
    @Log(module = "用户管理", operation = "查询用户详情")
    public Result<User> getById(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('1')")
    @Log(module = "用户管理", operation = "新增用户")
    public Result<Boolean> create(@RequestBody @Valid UserDTO dto) {
        return Result.ok(userService.save(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('1')")
    @Log(module = "用户管理", operation = "编辑用户")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody UserDTO dto) {
        return Result.ok(userService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('1')")
    @Log(module = "用户管理", operation = "删除用户")
    public Result<Boolean> remove(@PathVariable Long id) {
        Long currentUserId = Long.parseLong(org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName());
        if (currentUserId.equals(id)) {
            return Result.fail("不能删除当前登录用户");
        }
        return Result.ok(userService.removeById(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('1')")
    @Log(module = "用户管理", operation = "修改用户状态")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Long currentUserId = Long.parseLong(org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName());
        if (currentUserId.equals(id) && status == Constants.USER_STATUS_DISABLED) {
            return Result.fail("不能禁用当前登录用户");
        }
        return Result.ok(userService.updateStatus(id, status));
    }
}
