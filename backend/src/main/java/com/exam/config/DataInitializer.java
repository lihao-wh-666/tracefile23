package com.exam.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.entity.Subject;
import com.exam.entity.User;
import com.exam.mapper.SubjectMapper;
import com.exam.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SubjectMapper subjectMapper;

    @Override
    public void run(String... args) {
        initAdminUser();
        initSubjects();
    }

    private void initAdminUser() {
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

    private void initSubjects() {
        String[] subjectNames = {
                "高等数学", "大学英语", "线性代数", "概率论与数理统计",
                "计算机基础", "C语言程序设计", "数据结构", "操作系统",
                "计算机网络", "数据库原理", "Java程序设计", "Python程序设计",
                "思想政治", "大学物理", "大学化学", "大学体育"
        };
        String[] subjectDescriptions = {
                "大学高等数学课程，涵盖微积分、极限、导数、积分等核心内容",
                "大学英语四级考试相关课程，包含听说读写综合训练",
                "线性代数课程，涵盖矩阵、行列式、向量空间、线性变换等内容",
                "概率论与数理统计课程，包含随机事件、随机变量、统计推断等",
                "计算机基础知识入门，涵盖计算机组成、操作系统、网络基础等",
                "C语言程序设计基础课程，学习结构化编程和算法实现",
                "数据结构与算法课程，涵盖链表、栈、队列、树、图等数据结构",
                "操作系统原理课程，学习进程管理、内存管理、文件系统等",
                "计算机网络课程，涵盖TCP/IP协议、HTTP、网络安全等内容",
                "数据库原理课程，学习SQL语言、关系型数据库设计与优化",
                "Java面向对象程序设计，学习Java语法、集合框架、多线程等",
                "Python程序设计，涵盖Python基础、数据分析、Web开发等",
                "思想政治教育课程，培养正确的世界观、人生观、价值观",
                "大学物理基础课程，涵盖力学、电磁学、光学、热学等内容",
                "大学化学基础课程，学习无机化学、有机化学基础知识",
                "大学体育课程，包含田径、球类、健身等体育运动训练"
        };

        for (int i = 0; i < subjectNames.length; i++) {
            Long count = subjectMapper.selectCount(
                    new LambdaQueryWrapper<Subject>().eq(Subject::getName, subjectNames[i]));
            if (count == null || count == 0) {
                Subject subject = new Subject();
                subject.setName(subjectNames[i]);
                subject.setDescription(subjectDescriptions[i]);
                subjectMapper.insert(subject);
            }
        }
    }
}
