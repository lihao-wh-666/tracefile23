package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.dto.QuestionDTO;
import com.exam.entity.Question;
import com.exam.entity.Subject;
import com.exam.mapper.QuestionMapper;
import com.exam.mapper.SubjectMapper;
import com.exam.service.QuestionService;
import com.exam.vo.QuestionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private SubjectMapper subjectMapper;

    @Override
    public IPage<QuestionVO> page(Integer current, Integer size, Long subjectId, Integer type, Integer difficulty) {
        Page<Question> page = new Page<>(current, size);
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (subjectId != null) {
            wrapper.eq(Question::getSubjectId, subjectId);
        }
        if (type != null) {
            wrapper.eq(Question::getType, type);
        }
        if (difficulty != null) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }
        wrapper.orderByDesc(Question::getCreateTime);
        IPage<Question> questionPage = questionMapper.selectPage(page, wrapper);

        IPage<QuestionVO> voPage = questionPage.convert(q -> {
            QuestionVO vo = new QuestionVO();
            BeanUtils.copyProperties(q, vo);
            return vo;
        });

        List<Long> subjectIds = voPage.getRecords().stream()
                .map(QuestionVO::getSubjectId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!subjectIds.isEmpty()) {
            List<Subject> subjects = subjectMapper.selectBatchIds(subjectIds);
            Map<Long, String> subjectNameMap = subjects.stream()
                    .collect(Collectors.toMap(Subject::getId, Subject::getName));
            voPage.getRecords().forEach(vo -> vo.setSubjectName(subjectNameMap.get(vo.getSubjectId())));
        }

        return voPage;
    }

    @Override
    public QuestionVO getDetail(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            return null;
        }
        QuestionVO vo = new QuestionVO();
        BeanUtils.copyProperties(question, vo);
        Subject subject = subjectMapper.selectById(question.getSubjectId());
        if (subject != null) {
            vo.setSubjectName(subject.getName());
        }
        return vo;
    }

    @Override
    public boolean save(QuestionDTO dto) {
        Question entity = new Question();
        BeanUtils.copyProperties(dto, entity);
        return questionMapper.insert(entity) > 0;
    }

    @Override
    public boolean update(Long id, QuestionDTO dto) {
        Question entity = new Question();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        return questionMapper.updateById(entity) > 0;
    }

    @Override
    public boolean removeById(Long id) {
        return questionMapper.deleteById(id) > 0;
    }
}
