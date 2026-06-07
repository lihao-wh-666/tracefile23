package com.exam.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.entity.User;
import com.exam.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void run(String... args) {
        User admin = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, "admin"));
        if (admin == null) {
            User user = new User();
            user.setUsername("admin");
            user.setPassword(new BCryptPasswordEncoder().encode("admin123"));
            user.setRealName("系统管理员");
            user.setRole(1);
            userMapper.insert(user);
        } else {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (!encoder.matches("admin123", admin.getPassword())) {
                admin.setPassword(encoder.encode("admin123"));
                userMapper.updateById(admin);
            }
        }
    }
}
