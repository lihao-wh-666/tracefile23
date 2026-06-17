package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.BusinessException;
import com.exam.common.ErrorCode;
import com.exam.dto.ExamSwitchRecordDTO;
import com.exam.entity.ExamRecord;
import com.exam.entity.ExamSwitchRecord;
import com.exam.mapper.ExamRecordMapper;
import com.exam.mapper.ExamSwitchRecordMapper;
import com.exam.service.ExamSwitchRecordService;
import com.exam.service.SystemConfigService;
import com.exam.vo.ExamSwitchStatisticsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ExamSwitchRecordServiceImpl implements ExamSwitchRecordService {

    @Autowired
    private ExamSwitchRecordMapper switchRecordMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private SystemConfigService systemConfigService;

    @Override
    @Transactional
    public Boolean recordSwitch(ExamSwitchRecordDTO dto, Long userId) {
        ExamRecord record = examRecordMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "考试记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限操作此记录");
        }

        ExamSwitchRecord switchRecord = new ExamSwitchRecord();
        switchRecord.setRecordId(dto.getRecordId());
        switchRecord.setUserId(userId);
        switchRecord.setExamId(record.getExamId());
        switchRecord.setSwitchType(dto.getSwitchType());
        switchRecord.setSwitchTime(LocalDateTime.now());
        switchRecord.setDuration(dto.getDuration() != null ? dto.getDuration() : 0);
        switchRecord.setAppName(dto.getAppName());
        switchRecord.setScreenshotDetected(dto.getScreenshotDetected() != null ? dto.getScreenshotDetected() : 0);
        switchRecord.setScreenRecordDetected(dto.getScreenRecordDetected() != null ? dto.getScreenRecordDetected() : 0);
        switchRecord.setDetails(dto.getDetails());

        int result = switchRecordMapper.insert(switchRecord);

        updateRecordStats(record, dto);

        return result > 0;
    }

    private void updateRecordStats(ExamRecord record, ExamSwitchRecordDTO dto) {
        Integer switchType = dto.getSwitchType();
        boolean isScreenshot = dto.getScreenshotDetected() != null && dto.getScreenshotDetected() == 1;
        boolean isScreenRecord = dto.getScreenRecordDetected() != null && dto.getScreenRecordDetected() == 1;

        if (switchType >= 1 && switchType <= 3) {
            record.setSwitchCount(record.getSwitchCount() == null ? 1 : record.getSwitchCount() + 1);
            if (dto.getDuration() != null && dto.getDuration() > 0) {
                record.setTotalSwitchDuration(
                    (record.getTotalSwitchDuration() == null ? 0 : record.getTotalSwitchDuration()) + dto.getDuration());
            }
        }

        if (isScreenshot) {
            record.setScreenshotCount(record.getScreenshotCount() == null ? 1 : record.getScreenshotCount() + 1);
        }

        if (isScreenRecord) {
            record.setScreenRecordCount(record.getScreenRecordCount() == null ? 1 : record.getScreenRecordCount() + 1);
        }

        examRecordMapper.updateById(record);
    }

    @Override
    public List<ExamSwitchRecord> getByRecordId(Long recordId, Long userId, Integer userRole) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "考试记录不存在");
        }
        if (userRole != null && userRole == 3 && !record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限查看此记录");
        }
        return switchRecordMapper.selectByRecordId(recordId);
    }

    @Override
    public ExamSwitchStatisticsVO getStatistics(Long recordId, Long userId, Integer userRole) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "考试记录不存在");
        }
        if (userRole != null && userRole == 3 && !record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限查看此记录");
        }

        ExamSwitchStatisticsVO vo = new ExamSwitchStatisticsVO();
        vo.setSwitchCount(record.getSwitchCount() == null ? 0 : record.getSwitchCount());
        vo.setTotalSwitchDuration(record.getTotalSwitchDuration() == null ? 0 : record.getTotalSwitchDuration());
        vo.setScreenshotCount(record.getScreenshotCount() == null ? 0 : record.getScreenshotCount());
        vo.setScreenRecordCount(record.getScreenRecordCount() == null ? 0 : record.getScreenRecordCount());
        vo.setWarningCount(record.getWarningCount() == null ? 0 : record.getWarningCount());

        vo.setMaxSwitchCount(systemConfigService.getIntValueByKey("exam.max_switch_count", 3));
        vo.setMaxSingleSwitchDuration(systemConfigService.getIntValueByKey("exam.max_single_switch_duration", 30));
        vo.setMaxTotalSwitchDuration(systemConfigService.getIntValueByKey("exam.max_total_switch_duration", 60));
        vo.setScreenshotDetectionEnabled(Boolean.parseBoolean(systemConfigService.getValueByKey("exam.screenshot_detection_enabled")));
        vo.setScreenRecordDetectionEnabled(Boolean.parseBoolean(systemConfigService.getValueByKey("exam.screen_record_detection_enabled")));
        vo.setAutoSubmitOnExceed(Boolean.parseBoolean(systemConfigService.getValueByKey("exam.auto_submit_on_exceed")));

        return vo;
    }

    @Override
    public IPage<ExamSwitchRecord> page(Integer current, Integer size, Long recordId, Long userId) {
        Page<ExamSwitchRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<ExamSwitchRecord> wrapper = new LambdaQueryWrapper<>();
        if (recordId != null) {
            wrapper.eq(ExamSwitchRecord::getRecordId, recordId);
        }
        if (userId != null) {
            wrapper.eq(ExamSwitchRecord::getUserId, userId);
        }
        wrapper.orderByDesc(ExamSwitchRecord::getSwitchTime);
        return switchRecordMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public Boolean incrementWarningCount(Long recordId, Long userId) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "考试记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限操作此记录");
        }
        record.setWarningCount(record.getWarningCount() == null ? 1 : record.getWarningCount() + 1);
        return examRecordMapper.updateById(record) > 0;
    }
}
