package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.dto.ExamDTO;
import com.exam.entity.Exam;
import com.exam.entity.Paper;
import com.exam.common.Constants;
import com.exam.mapper.ExamMapper;
import com.exam.mapper.PaperMapper;
import com.exam.service.ExamService;
import com.exam.vo.ExamVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ExamServiceImpl implements ExamService {

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private PaperMapper paperMapper;

    private int computeStatus(Exam exam) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(exam.getStartTime())) {
            return Constants.EXAM_NOT_START;
        } else if (now.isAfter(exam.getEndTime())) {
            return Constants.EXAM_END;
        } else {
            return Constants.EXAM_ING;
        }
    }

    private int computeStatus(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            return Constants.EXAM_NOT_START;
        } else if (now.isAfter(endTime)) {
            return Constants.EXAM_END;
        } else {
            return Constants.EXAM_ING;
        }
    }

    @Override
    public IPage<ExamVO> page(Integer current, Integer size, Integer status) {
        Page<Exam> page = new Page<>(current, size);
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Exam::getCreateTime);
        IPage<Exam> examPage = examMapper.selectPage(page, wrapper);

        IPage<ExamVO> voPage = examPage.convert(e -> {
            ExamVO vo = new ExamVO();
            BeanUtils.copyProperties(e, vo);
            vo.setStatus(computeStatus(e));
            return vo;
        });

        if (status != null) {
            voPage.setRecords(voPage.getRecords().stream()
                    .filter(vo -> vo.getStatus() != null && vo.getStatus().equals(status))
                    .collect(Collectors.toList()));
            voPage.setTotal(voPage.getRecords().size());
        }

        List<Long> paperIds = voPage.getRecords().stream()
                .map(ExamVO::getPaperId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!paperIds.isEmpty()) {
            List<Paper> papers = paperMapper.selectBatchIds(paperIds);
            Map<Long, String> paperNameMap = papers.stream()
                    .collect(Collectors.toMap(Paper::getId, Paper::getName));
            voPage.getRecords().forEach(vo -> vo.setPaperName(paperNameMap.get(vo.getPaperId())));
        }

        return voPage;
    }

    @Override
    public ExamVO getDetail(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) {
            return null;
        }
        ExamVO vo = new ExamVO();
        BeanUtils.copyProperties(exam, vo);
        vo.setStatus(computeStatus(exam));
        Paper paper = paperMapper.selectById(exam.getPaperId());
        if (paper != null) {
            vo.setPaperName(paper.getName());
        }
        return vo;
    }

    @Override
    public boolean save(ExamDTO dto) {
        Exam exam = new Exam();
        BeanUtils.copyProperties(dto, exam);
        exam.setStatus(computeStatus(dto.getStartTime(), dto.getEndTime()));
        return examMapper.insert(exam) > 0;
    }

    @Override
    public boolean update(Long id, ExamDTO dto) {
        Exam exam = new Exam();
        BeanUtils.copyProperties(dto, exam);
        exam.setId(id);
        exam.setStatus(computeStatus(dto.getStartTime(), dto.getEndTime()));
        return examMapper.updateById(exam) > 0;
    }

    @Override
    public boolean removeById(Long id) {
        return examMapper.deleteById(id) > 0;
    }
}
