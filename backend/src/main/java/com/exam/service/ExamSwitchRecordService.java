package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.dto.ExamSwitchRecordDTO;
import com.exam.entity.ExamSwitchRecord;
import com.exam.vo.ExamSwitchStatisticsVO;

import java.util.List;

public interface ExamSwitchRecordService {

    Boolean recordSwitch(ExamSwitchRecordDTO dto, Long userId);

    List<ExamSwitchRecord> getByRecordId(Long recordId, Long userId, Integer userRole);

    ExamSwitchStatisticsVO getStatistics(Long recordId, Long userId, Integer userRole);

    IPage<ExamSwitchRecord> page(Integer current, Integer size, Long recordId, Long userId);

    Boolean incrementWarningCount(Long recordId, Long userId);
}
