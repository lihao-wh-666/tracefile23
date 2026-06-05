package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.entity.Subject;
import com.exam.mapper.SubjectMapper;
import com.exam.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubjectServiceImpl implements SubjectService {

    @Autowired
    private SubjectMapper subjectMapper;

    @Override
    public IPage<Subject> page(Integer current, Integer size, String name) {
        Page<Subject> page = new Page<>(current, size);
        LambdaQueryWrapper<Subject> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Subject::getName, name);
        }
        wrapper.orderByDesc(Subject::getCreateTime);
        return subjectMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Subject> list() {
        return subjectMapper.selectList(null);
    }

    @Override
    public Subject getById(Long id) {
        return subjectMapper.selectById(id);
    }

    @Override
    public boolean save(Subject entity) {
        return subjectMapper.insert(entity) > 0;
    }

    @Override
    public boolean updateById(Subject entity) {
        return subjectMapper.updateById(entity) > 0;
    }

    @Override
    public boolean removeById(Long id) {
        return subjectMapper.deleteById(id) > 0;
    }
}
