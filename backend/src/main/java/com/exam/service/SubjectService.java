package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.entity.Subject;

import java.util.List;

public interface SubjectService {

    IPage<Subject> page(Integer current, Integer size, String name);

    List<Subject> list();

    Subject getById(Long id);

    boolean save(Subject entity);

    boolean updateById(Subject entity);

    boolean removeById(Long id);
}
