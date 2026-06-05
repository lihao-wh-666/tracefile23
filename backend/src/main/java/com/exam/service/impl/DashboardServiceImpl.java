package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.entity.Exam;
import com.exam.entity.Paper;
import com.exam.entity.User;
import com.exam.mapper.ExamMapper;
import com.exam.mapper.PaperMapper;
import com.exam.mapper.QuestionMapper;
import com.exam.mapper.SubjectMapper;
import com.exam.mapper.UserMapper;
import com.exam.service.DashboardService;
import com.exam.vo.DashboardVO;
import com.exam.vo.ExamVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private PaperMapper paperMapper;

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public DashboardVO getData() {
        DashboardVO vo = new DashboardVO();
        vo.setSubjectCount(subjectMapper.selectCount(null));
        vo.setQuestionCount(questionMapper.selectCount(null));
        vo.setPaperCount(paperMapper.selectCount(null));
        vo.setExamCount(examMapper.selectCount(null));
        vo.setUserCount(userMapper.selectCount(null));

        List<Exam> recentExams = examMapper.selectList(
                new LambdaQueryWrapper<Exam>()
                        .orderByDesc(Exam::getCreateTime)
                        .last("limit 5"));
        if (!recentExams.isEmpty()) {
            List<Long> paperIds = recentExams.stream()
                    .map(Exam::getPaperId)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, String> paperNameMap = paperMapper.selectBatchIds(paperIds).stream()
                    .collect(Collectors.toMap(Paper::getId, Paper::getName));

            List<ExamVO> examVOs = recentExams.stream().map(e -> {
                ExamVO evo = new ExamVO();
                BeanUtils.copyProperties(e, evo);
                evo.setPaperName(paperNameMap.get(e.getPaperId()));
                return evo;
            }).collect(Collectors.toList());
            vo.setRecentExams(examVOs);
        }

        return vo;
    }
}
