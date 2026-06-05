package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.dto.ExamDTO;
import com.exam.vo.ExamVO;

public interface ExamService {

    IPage<ExamVO> page(Integer current, Integer size, Integer status);

    ExamVO getDetail(Long id);

    boolean save(ExamDTO dto);

    boolean update(Long id, ExamDTO dto);

    boolean removeById(Long id);
}
