package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.dto.QuestionDTO;
import com.exam.vo.QuestionVO;

public interface QuestionService {

    IPage<QuestionVO> page(Integer current, Integer size, Long subjectId, Integer type, Integer difficulty);

    QuestionVO getDetail(Long id);

    boolean save(QuestionDTO dto);

    boolean update(Long id, QuestionDTO dto);

    boolean removeById(Long id);
}
