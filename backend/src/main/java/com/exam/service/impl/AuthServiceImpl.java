package com.exam.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.asymmetric.RSA;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.common.BusinessException;
import com.exam.common.Constants;
import com.exam.common.ErrorCode;
import com.exam.dto.LoginDTO;
import com.exam.dto.RegisterDTO;
import com.exam.dto.ResetPasswordDTO;
import com.exam.dto.SendCodeDTO;
import com.exam.entity.User;
import com.exam.mapper.UserMapper;
import com.exam.security.JwtUtils;
import com.exam.service.AuthService;
import com.exam.service.SystemConfigService;
import com.exam.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private RSA rsa;

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisTemplate<String, Object> objectRedisTemplate;

    @Autowired
    private SystemConfigService systemConfigService;

    private static final String CODE_PREFIX = "auth:code:";
    private static final int CODE_EXPIRE_MINUTES = 5;

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR);
        }

        String lockKey = Constants.LOGIN_LOCK_PREFIX + user.getId();
        String lockValue = redisTemplate != null ? redisTemplate.opsForValue().get(lockKey) : null;
        if (lockValue != null) {
            long remainSeconds = redisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
            long remainMinutes = remainSeconds / 60 + (remainSeconds % 60 > 0 ? 1 : 0);
            throw new BusinessException(ErrorCode.USER_LOGIN_LOCKED.getCode(),
                    "登录失败次数过多，账号已被锁定，请" + remainMinutes + "分钟后再试");
        }

        String decryptedPassword;
        try {
            decryptedPassword = rsa.decryptStr(dto.getPassword(), cn.hutool.crypto.asymmetric.KeyType.PrivateKey);
        } catch (Exception e) {
            throw new BusinessException("密码解密失败");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(decryptedPassword, user.getPassword())) {
            int maxErrorCount = systemConfigService.getIntValueByKey(
                    Constants.CONFIG_LOGIN_MAX_ERROR_COUNT,
                    Constants.DEFAULT_LOGIN_MAX_ERROR_COUNT
            );
            String errorCountKey = Constants.LOGIN_ERROR_COUNT_PREFIX + user.getId();
            String errorCountStr = redisTemplate != null ? redisTemplate.opsForValue().get(errorCountKey) : null;
            int errorCount = errorCountStr != null ? Integer.parseInt(errorCountStr) + 1 : 1;

            if (errorCount >= maxErrorCount) {
                int lockDuration = systemConfigService.getIntValueByKey(
                        Constants.CONFIG_LOGIN_LOCK_DURATION,
                        Constants.DEFAULT_LOGIN_LOCK_DURATION_MINUTES
                );
                if (redisTemplate != null) {
                    redisTemplate.opsForValue().set(lockKey, String.valueOf(errorCount), lockDuration, TimeUnit.MINUTES);
                    redisTemplate.delete(errorCountKey);
                }
                throw new BusinessException(ErrorCode.USER_LOGIN_LOCKED.getCode(),
                        "密码错误次数超过限制，账号已被锁定" + lockDuration + "分钟");
            } else {
                if (redisTemplate != null) {
                    redisTemplate.opsForValue().set(errorCountKey, String.valueOf(errorCount), 30, TimeUnit.MINUTES);
                }
                int remaining = maxErrorCount - errorCount;
                throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR.getCode(),
                        "用户名或密码错误，还可尝试" + remaining + "次");
            }
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }

        String errorCountKey = Constants.LOGIN_ERROR_COUNT_PREFIX + user.getId();
        if (redisTemplate != null) {
            redisTemplate.delete(errorCountKey);
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

        int timeoutMinutes = systemConfigService.getIntValueByKey(
                Constants.CONFIG_LOGIN_TIMEOUT,
                Constants.DEFAULT_LOGIN_TIMEOUT_MINUTES
        );
        String sessionKey = Constants.SESSION_LAST_ACTIVITY_PREFIX + user.getId();
        objectRedisTemplate.opsForValue().set(
                sessionKey,
                String.valueOf(System.currentTimeMillis()),
                timeoutMinutes + 5,
                TimeUnit.MINUTES
        );

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setAvatar(user.getAvatar());
        return vo;
    }

    @Override
    public void logout(Long userId) {
        if (userId != null) {
            String sessionKey = Constants.SESSION_LAST_ACTIVITY_PREFIX + userId;
            objectRedisTemplate.delete(sessionKey);
        }
    }

    @Override
    public void register(RegisterDTO dto) {
        User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
        if (existUser != null) {
            throw new BusinessException("邮箱已被注册");
        }

        String decryptedPassword;
        try {
            decryptedPassword = rsa.decryptStr(dto.getPassword(), cn.hutool.crypto.asymmetric.KeyType.PrivateKey);
        } catch (Exception e) {
            throw new BusinessException("密码解密失败");
        }

        String decryptedConfirmPassword;
        try {
            decryptedConfirmPassword = rsa.decryptStr(dto.getConfirmPassword(), cn.hutool.crypto.asymmetric.KeyType.PrivateKey);
        } catch (Exception e) {
            throw new BusinessException("确认密码解密失败");
        }

        if (!decryptedPassword.equals(decryptedConfirmPassword)) {
            throw new BusinessException("两次输入的密码不一致");
        }

        if (decryptedPassword.length() < 6 || decryptedPassword.length() > 20) {
            throw new BusinessException("密码长度必须在6-20之间");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(encoder.encode(decryptedPassword));
        user.setRealName(dto.getUsername());
        user.setRole(2);
        user.setAvatar("");
        userMapper.insert(user);
    }

    @Override
    public void sendCode(SendCodeDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
        if (user == null) {
            throw new BusinessException("该邮箱未注册");
        }

        String code = RandomUtil.randomNumbers(6);

        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(CODE_PREFIX + dto.getEmail(), code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }

        System.out.println("验证码已发送至 " + dto.getEmail() + "，验证码为：" + code);
    }

    @Override
    public void resetPassword(ResetPasswordDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
        if (user == null) {
            throw new BusinessException("该邮箱未注册");
        }

        if (redisTemplate != null) {
            String storedCode = redisTemplate.opsForValue().get(CODE_PREFIX + dto.getEmail());
            if (storedCode == null || !storedCode.equals(dto.getCode())) {
                throw new BusinessException("验证码错误或已过期");
            }
            redisTemplate.delete(CODE_PREFIX + dto.getEmail());
        } else {
            if (!"123456".equals(dto.getCode())) {
                throw new BusinessException("验证码错误（测试阶段请使用123456）");
            }
        }

        String decryptedPassword;
        try {
            decryptedPassword = rsa.decryptStr(dto.getNewPassword(), cn.hutool.crypto.asymmetric.KeyType.PrivateKey);
        } catch (Exception e) {
            throw new BusinessException("密码解密失败");
        }

        String decryptedConfirmPassword;
        try {
            decryptedConfirmPassword = rsa.decryptStr(dto.getConfirmPassword(), cn.hutool.crypto.asymmetric.KeyType.PrivateKey);
        } catch (Exception e) {
            throw new BusinessException("确认密码解密失败");
        }

        if (!decryptedPassword.equals(decryptedConfirmPassword)) {
            throw new BusinessException("两次输入的密码不一致");
        }

        if (decryptedPassword.length() < 6 || decryptedPassword.length() > 20) {
            throw new BusinessException("密码长度必须在6-20之间");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(decryptedPassword));
        userMapper.updateById(user);
    }
}
