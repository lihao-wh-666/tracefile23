package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.dto.PaperDTO;
import com.exam.vo.PaperVO;

public interface PaperService {

    IPage<PaperVO> page(Integer current, Integer size, Long subjectId, Integer status);

    PaperVO getDetail(Long id);

    boolean save(PaperDTO dto);

    boolean update(Long id, PaperDTO dto);

    boolean removeById(Long id);

    boolean publish(Long id);
}
