package com.exam.controller;

import com.exam.common.Result;
import com.exam.dto.UserDTO;
import com.exam.entity.User;
import com.exam.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/info")
    public Result<User> info() {
        Long userId = Long.parseLong(SecurityContextHolder.getContext().getAuthentication().getName());
        User user = userMapper.selectById(userId);
        user.setPassword(null);
        return Result.ok(user);
    }

    @PostMapping
    public Result<Boolean> create(@RequestBody @Valid UserDTO dto) {
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setRole(dto.getRole());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        return Result.ok(userMapper.insert(user) > 0);
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody UserDTO dto) {
        User user = new User();
        user.setId(id);
        user.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setRealName(dto.getRealName());
        user.setRole(dto.getRole());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        return Result.ok(userMapper.updateById(user) > 0);
    }
}
