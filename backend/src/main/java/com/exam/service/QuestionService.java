package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.dto.QuestionDTO;
import com.exam.vo.QuestionImportVO;
import com.exam.vo.QuestionVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

public interface QuestionService {

    IPage<QuestionVO> page(Integer current, Integer size, Long subjectId, Integer type, Integer difficulty);

    QuestionVO getDetail(Long id);

    boolean save(QuestionDTO dto);

    boolean update(Long id, QuestionDTO dto);

    boolean removeById(Long id);

    QuestionImportVO importQuestions(MultipartFile file, Long subjectId);

    void downloadTemplate(HttpServletResponse response);
}
