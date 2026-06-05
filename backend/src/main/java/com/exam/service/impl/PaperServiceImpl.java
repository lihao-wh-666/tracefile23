package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.dto.PaperDTO;
import com.exam.entity.Paper;
import com.exam.entity.PaperQuestion;
import com.exam.entity.Question;
import com.exam.entity.Subject;
import com.exam.mapper.PaperMapper;
import com.exam.mapper.PaperQuestionMapper;
import com.exam.mapper.QuestionMapper;
import com.exam.mapper.SubjectMapper;
import com.exam.service.PaperService;
import com.exam.vo.PaperVO;
import com.exam.vo.QuestionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class PaperServiceImpl implements PaperService {

    @Autowired
    private PaperMapper paperMapper;

    @Autowired
    private PaperQuestionMapper paperQuestionMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private SubjectMapper subjectMapper;

    @Override
    public IPage<PaperVO> page(Integer current, Integer size, Long subjectId, Integer status) {
        Page<Paper> page = new Page<>(current, size);
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
        if (subjectId != null) {
            wrapper.eq(Paper::getSubjectId, subjectId);
        }
        if (status != null) {
            wrapper.eq(Paper::getStatus, status);
        }
        wrapper.orderByDesc(Paper::getCreateTime);
        IPage<Paper> paperPage = paperMapper.selectPage(page, wrapper);

        IPage<PaperVO> voPage = paperPage.convert(p -> {
            PaperVO vo = new PaperVO();
            BeanUtils.copyProperties(p, vo);
            return vo;
        });

        List<Long> subjectIds = voPage.getRecords().stream()
                .map(PaperVO::getSubjectId)
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
    public PaperVO getDetail(Long id) {
        Paper paper = paperMapper.selectById(id);
        if (paper == null) {
            return null;
        }
        PaperVO vo = new PaperVO();
        BeanUtils.copyProperties(paper, vo);

        Subject subject = subjectMapper.selectById(paper.getSubjectId());
        if (subject != null) {
            vo.setSubjectName(subject.getName());
        }

        List<PaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getPaperId, id)
                        .orderByAsc(PaperQuestion::getSort));
        if (!paperQuestions.isEmpty()) {
            List<Long> questionIds = paperQuestions.stream()
                    .map(PaperQuestion::getQuestionId)
                    .collect(Collectors.toList());
            List<Question> questions = questionMapper.selectBatchIds(questionIds);
            Map<Long, Question> questionMap = questions.stream()
                    .collect(Collectors.toMap(Question::getId, q -> q));

            List<QuestionVO> questionVOs = paperQuestions.stream().map(pq -> {
                Question q = questionMap.get(pq.getQuestionId());
                if (q != null) {
                    QuestionVO qvo = new QuestionVO();
                    BeanUtils.copyProperties(q, qvo);
                    qvo.setSubjectName(subject != null ? subject.getName() : null);
                    return qvo;
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());

            vo.setQuestions(questionVOs);
        }

        return vo;
    }

    @Override
    public boolean save(PaperDTO dto) {
        Paper paper = new Paper();
        BeanUtils.copyProperties(dto, paper);
        paper.setStatus(0);
        paperMapper.insert(paper);

        if (dto.getQuestionIds() != null) {
            for (int i = 0; i < dto.getQuestionIds().size(); i++) {
                PaperQuestion pq = new PaperQuestion();
                pq.setPaperId(paper.getId());
                pq.setQuestionId(dto.getQuestionIds().get(i));
                pq.setSort(i + 1);
                paperQuestionMapper.insert(pq);
            }
        }

        return true;
    }

    @Override
    public boolean update(Long id, PaperDTO dto) {
        Paper paper = new Paper();
        BeanUtils.copyProperties(dto, paper);
        paper.setId(id);
        paperMapper.updateById(paper);

        paperQuestionMapper.delete(
                new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, id));

        if (dto.getQuestionIds() != null) {
            for (int i = 0; i < dto.getQuestionIds().size(); i++) {
                PaperQuestion pq = new PaperQuestion();
                pq.setPaperId(id);
                pq.setQuestionId(dto.getQuestionIds().get(i));
                pq.setSort(i + 1);
                paperQuestionMapper.insert(pq);
            }
        }

        return true;
    }

    @Override
    public boolean removeById(Long id) {
        paperQuestionMapper.delete(
                new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, id));
        return paperMapper.deleteById(id) > 0;
    }

    @Override
    public boolean publish(Long id) {
        Paper paper = new Paper();
        paper.setId(id);
        paper.setStatus(1);
        return paperMapper.updateById(paper) > 0;
    }
}
