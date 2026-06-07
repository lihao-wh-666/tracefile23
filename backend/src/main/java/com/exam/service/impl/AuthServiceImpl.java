package com.exam.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.asymmetric.RSA;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.common.BusinessException;
import com.exam.common.ErrorCode;
import com.exam.dto.LoginDTO;
import com.exam.dto.RegisterDTO;
import com.exam.dto.ResetPasswordDTO;
import com.exam.dto.SendCodeDTO;
import com.exam.entity.User;
import com.exam.mapper.UserMapper;
import com.exam.security.JwtUtils;
import com.exam.service.AuthService;
import com.exam.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
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

    private static final String CODE_PREFIX = "auth:code:";
    private static final int CODE_EXPIRE_MINUTES = 5;

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR);
        }
        String decryptedPassword;
        try {
            decryptedPassword = rsa.decryptStr(dto.getPassword(), cn.hutool.crypto.asymmetric.KeyType.PrivateKey);
        } catch (Exception e) {
            throw new BusinessException("密码解密失败");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(decryptedPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR);
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
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
    public void register(RegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

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
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }

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

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(decryptedPassword));
        userMapper.updateById(user);
    }
}
