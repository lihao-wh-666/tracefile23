package com.exam.service.impl;

import cn.hutool.crypto.asymmetric.RSA;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.BusinessException;
import com.exam.common.Constants;
import com.exam.common.ErrorCode;
import com.exam.dto.ChangePasswordDTO;
import com.exam.dto.UpdateProfileDTO;
import com.exam.dto.UserDTO;
import com.exam.entity.User;
import com.exam.mapper.UserMapper;
import com.exam.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RSA rsa;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public IPage<User> page(Integer current, Integer size, String keyword, Integer role, Integer status) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getPhone, keyword)
                    .or().like(User::getEmail, keyword));
        }
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);
        IPage<User> userPage = userMapper.selectPage(page, wrapper);
        userPage.getRecords().forEach(user -> user.setPassword(null));
        return userPage;
    }

    @Override
    public User getById(Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    @Override
    public boolean save(UserDTO dto) {
        if (dto.getPassword() == null || dto.getPassword().isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        User existUser = userMapper.selectByUsername(dto.getUsername());
        if (existUser != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXIST);
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        String decryptedPassword = rsa.decryptStr(dto.getPassword(), cn.hutool.crypto.asymmetric.KeyType.PrivateKey);
        user.setPassword(passwordEncoder.encode(decryptedPassword));
        user.setRealName(dto.getRealName());
        user.setRole(dto.getRole());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(1);
        return userMapper.insert(user) > 0;
    }

    @Override
    public boolean update(Long id, UserDTO dto) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        if (!user.getUsername().equals(dto.getUsername())) {
            User existUser = userMapper.selectByUsername(dto.getUsername());
            if (existUser != null) {
                throw new BusinessException(ErrorCode.USER_ALREADY_EXIST);
            }
        }
        User updateUser = new User();
        updateUser.setId(id);
        updateUser.setUsername(dto.getUsername());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            String decryptedPassword = rsa.decryptStr(dto.getPassword(), cn.hutool.crypto.asymmetric.KeyType.PrivateKey);
            updateUser.setPassword(passwordEncoder.encode(decryptedPassword));
        }
        updateUser.setRealName(dto.getRealName());
        updateUser.setRole(dto.getRole());
        updateUser.setEmail(dto.getEmail());
        updateUser.setPhone(dto.getPhone());
        return userMapper.updateById(updateUser) > 0;
    }

    @Override
    public boolean removeById(Long id) {
        return userMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean unlockUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        String lockKey = Constants.LOGIN_LOCK_PREFIX + id;
        String errorCountKey = Constants.LOGIN_ERROR_COUNT_PREFIX + id;
        redisTemplate.delete(lockKey);
        redisTemplate.delete(errorCountKey);

        User updateUser = new User();
        updateUser.setId(id);
        updateUser.setLoginLocked(Constants.LOGIN_LOCKED_NO);
        updateUser.setLockEndTime(null);
        return userMapper.updateById(updateUser) > 0;
    }

    @Override
    public boolean updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setRealName(dto.getRealName());
        updateUser.setPhone(dto.getPhone());
        updateUser.setEmail(dto.getEmail());
        return userMapper.updateById(updateUser) > 0;
    }

    @Override
    public boolean changePassword(Long userId, ChangePasswordDTO dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }
        if (dto.getNewPassword().length() < 6) {
            throw new BusinessException("新密码长度不能少于6位");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        String decryptedOldPassword = rsa.decryptStr(dto.getOldPassword(), cn.hutool.crypto.asymmetric.KeyType.PrivateKey);
        if (!passwordEncoder.matches(decryptedOldPassword, user.getPassword())) {
            throw new BusinessException("旧密码不正确");
        }
        String decryptedNewPassword = rsa.decryptStr(dto.getNewPassword(), cn.hutool.crypto.asymmetric.KeyType.PrivateKey);
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(passwordEncoder.encode(decryptedNewPassword));
        return userMapper.updateById(updateUser) > 0;
    }

    @Override
    public String updateAvatar(Long userId, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setAvatar(avatarUrl);
        userMapper.updateById(updateUser);
        return avatarUrl;
    }
}
