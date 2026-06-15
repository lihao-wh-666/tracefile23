package com.exam.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.annotation.Log;
import com.exam.common.BusinessException;
import com.exam.common.Constants;
import com.exam.common.ErrorCode;
import com.exam.common.Result;
import com.exam.dto.ChangePasswordDTO;
import com.exam.dto.UpdateProfileDTO;
import com.exam.dto.UserDTO;
import com.exam.entity.User;
import com.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    @GetMapping("/info")
    public Result<User> info() {
        Long userId = getCurrentUserId();
        return Result.ok(userService.getById(userId));
    }

    @PutMapping("/profile")
    @Log(module = "个人中心", operation = "更新个人信息")
    public Result<Boolean> updateProfile(@RequestBody @Valid UpdateProfileDTO dto) {
        Long userId = getCurrentUserId();
        return Result.ok(userService.updateProfile(userId, dto));
    }

    @PutMapping("/password")
    @Log(module = "个人中心", operation = "修改密码")
    public Result<Boolean> changePassword(@RequestBody @Valid ChangePasswordDTO dto) {
        Long userId = getCurrentUserId();
        return Result.ok(userService.changePassword(userId, dto));
    }

    @PostMapping("/avatar")
    @Log(module = "个人中心", operation = "上传头像")
    public Result<String> uploadAvatar(@RequestPart("file") MultipartFile file) throws IOException {
        Long userId = getCurrentUserId();
        if (file.isEmpty()) {
            return Result.fail("请选择要上传的文件");
        }
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            return Result.fail("文件名不能为空");
        }
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex < 0) {
            return Result.fail("文件格式不正确");
        }
        String extension = originalFilename.substring(dotIndex);
        if (!".jpg".equalsIgnoreCase(extension) && !".jpeg".equalsIgnoreCase(extension)
                && !".png".equalsIgnoreCase(extension) && !".gif".equalsIgnoreCase(extension)) {
            return Result.fail("只支持 jpg、jpeg、png、gif 格式的图片");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.fail("图片大小不能超过 5MB");
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        String uploadDirPath = System.getProperty("user.dir") + "/uploads/avatar";
        File uploadDir = new File(uploadDirPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        File destFile = new File(uploadDirPath + "/" + fileName);
        java.nio.file.Files.copy(file.getInputStream(), destFile.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        String avatarUrl = "/uploads/avatar/" + fileName;
        userService.updateAvatar(userId, avatarUrl);
        return Result.ok(avatarUrl);
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
        Long currentUserId = getCurrentUserId();
        if (currentUserId.equals(id)) {
            return Result.fail("不能删除当前登录用户");
        }
        return Result.ok(userService.removeById(id));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('1')")
    @Log(module = "用户管理", operation = "修改用户状态")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId.equals(id) && status == Constants.USER_STATUS_DISABLED) {
            return Result.fail("不能禁用当前登录用户");
        }
        return Result.ok(userService.updateStatus(id, status));
    }

    @PutMapping("/{id}/unlock")
    @PreAuthorize("hasRole('1')")
    @Log(module = "用户管理", operation = "解锁用户")
    public Result<Boolean> unlockUser(@PathVariable Long id) {
        return Result.ok(userService.unlockUser(id));
    }
}
