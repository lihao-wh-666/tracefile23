package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Constants;
import com.exam.dto.SubmitExamDTO;
import com.exam.entity.*;
import com.exam.mapper.*;
import com.exam.service.ExamRecordService;
import com.exam.vo.ExamRecordVO;
import com.exam.vo.ScoreStatVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamRecordServiceImpl implements ExamRecordService {

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private ExamMapper examMapper;

    @Autowired
    private PaperMapper paperMapper;

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private PaperQuestionMapper paperQuestionMapper;

    @Autowired
    private ExamAnswerMapper examAnswerMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public IPage<ExamRecordVO> page(Integer current, Integer size, Long examId, Long userId, Integer status) {
        Page<ExamRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        if (examId != null) {
            wrapper.eq(ExamRecord::getExamId, examId);
        }
        if (userId != null) {
            wrapper.eq(ExamRecord::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(ExamRecord::getStatus, status);
        }
        wrapper.orderByDesc(ExamRecord::getStartTime);
        IPage<ExamRecord> recordPage = examRecordMapper.selectPage(page, wrapper);

        IPage<ExamRecordVO> voPage = recordPage.convert(r -> buildRecordVO(r));

        fillBatchInfo(voPage.getRecords());

        return voPage;
    }

    @Override
    public ExamRecordVO getDetail(Long id) {
        ExamRecord record = examRecordMapper.selectById(id);
        if (record == null) {
            return null;
        }
        ExamRecordVO vo = buildRecordVO(record);
        fillBatchInfo(Collections.singletonList(vo));
        return vo;
    }

    @Override
    public ExamRecordVO startExam(Long examId, Long userId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new RuntimeException("考试不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(exam.getStartTime()) || now.isAfter(exam.getEndTime())) {
            throw new RuntimeException("考试未在进行中");
        }

        ExamRecord existing = examRecordMapper.selectOne(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getExamId, examId)
                        .eq(ExamRecord::getUserId, userId));
        if (existing != null) {
            throw new RuntimeException("您已开始过该考试");
        }

        ExamRecord record = new ExamRecord();
        record.setExamId(examId);
        record.setUserId(userId);
        record.setPaperId(exam.getPaperId());
        record.setStartTime(now);
        record.setStatus(Constants.RECORD_EXAMING);
        examRecordMapper.insert(record);

        List<PaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getPaperId, exam.getPaperId())
                        .orderByAsc(PaperQuestion::getSort));
        for (PaperQuestion pq : paperQuestions) {
            ExamAnswer answer = new ExamAnswer();
            answer.setRecordId(record.getId());
            answer.setQuestionId(pq.getQuestionId());
            answer.setAnswer("");
            answer.setIsCorrect(0);
            answer.setScore(0);
            answer.setAutoScore(0);
            examAnswerMapper.insert(answer);
        }

        ExamRecordVO vo = buildRecordVO(record);
        fillBatchInfo(Collections.singletonList(vo));
        return vo;
    }

    @Override
    public ExamRecordVO submitExam(SubmitExamDTO dto, Long userId) {
        ExamRecord record = examRecordMapper.selectOne(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getExamId, dto.getExamId())
                        .eq(ExamRecord::getUserId, userId)
                        .eq(ExamRecord::getStatus, Constants.RECORD_EXAMING));
        if (record == null) {
            throw new RuntimeException("考试记录不存在");
        }

        record.setSubmitTime(LocalDateTime.now());
        record.setStatus(Constants.RECORD_SUBMITTED);

        List<ExamAnswer> answers = examAnswerMapper.selectList(
                new LambdaQueryWrapper<ExamAnswer>().eq(ExamAnswer::getRecordId, record.getId()));
        Map<Long, ExamAnswer> answerMap = answers.stream()
                .collect(Collectors.toMap(ExamAnswer::getQuestionId, a -> a));

        List<Long> questionIds = answers.stream().map(ExamAnswer::getQuestionId).collect(Collectors.toList());
        Map<Long, Question> questionMap = new HashMap<>();
        if (!questionIds.isEmpty()) {
            questionMapper.selectBatchIds(questionIds).forEach(q -> questionMap.put(q.getId(), q));
        }

        int totalAutoScore = 0;
        if (dto.getAnswers() != null) {
            for (SubmitExamDTO.AnswerItem item : dto.getAnswers()) {
                ExamAnswer examAnswer = answerMap.get(item.getQuestionId());
                if (examAnswer == null) {
                    continue;
                }
                examAnswer.setAnswer(item.getAnswer());

                Question question = questionMap.get(item.getQuestionId());
                if (question == null) {
                    examAnswer.setAutoScore(0);
                    examAnswer.setIsCorrect(0);
                    examAnswer.setScore(0);
                    examAnswerMapper.updateById(examAnswer);
                    continue;
                }

                int autoScore = autoGrade(question, item.getAnswer());
                examAnswer.setAutoScore(autoScore);
                if (question.getType() == Constants.TYPE_ESSAY) {
                    examAnswer.setIsCorrect(2);
                    examAnswer.setScore(0);
                } else {
                    examAnswer.setIsCorrect(autoScore > 0 ? 1 : 0);
                    examAnswer.setScore(autoScore);
                }
                totalAutoScore += autoScore;
                examAnswerMapper.updateById(examAnswer);
            }
        }

        record.setScore(totalAutoScore);
        examRecordMapper.updateById(record);

        ExamRecordVO vo = buildRecordVO(record);
        fillBatchInfo(Collections.singletonList(vo));
        return vo;
    }

    @Override
    public List<ScoreStatVO> scoreStats(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new RuntimeException("考试不存在");
        }
        Paper paper = paperMapper.selectById(exam.getPaperId());
        int passScore = paper != null ? paper.getPassScore() : 0;

        List<ExamRecord> records = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getExamId, examId)
                        .eq(ExamRecord::getStatus, Constants.RECORD_SUBMITTED)
                        .or()
                        .eq(ExamRecord::getStatus, Constants.RECORD_GRADED));

        ScoreStatVO stat = new ScoreStatVO();
        stat.setExamId(examId);
        stat.setExamName(exam.getName());
        stat.setTotalCount(records.size());

        if (records.isEmpty()) {
            stat.setAvgScore(0.0);
            stat.setMaxScore(0);
            stat.setMinScore(0);
            stat.setPassCount(0);
            stat.setPassRate(0.0);
        } else {
            double avg = records.stream().mapToInt(ExamRecord::getScore).average().orElse(0.0);
            int max = records.stream().mapToInt(ExamRecord::getScore).max().orElse(0);
            int min = records.stream().mapToInt(ExamRecord::getScore).min().orElse(0);
            long passCount = records.stream().filter(r -> r.getScore() >= passScore).count();
            double passRate = (double) passCount / records.size() * 100;

            stat.setAvgScore(Math.round(avg * 100.0) / 100.0);
            stat.setMaxScore(max);
            stat.setMinScore(min);
            stat.setPassCount((int) passCount);
            stat.setPassRate(Math.round(passRate * 100.0) / 100.0);
        }

        return Collections.singletonList(stat);
    }

    private int autoGrade(Question question, String userAnswer) {
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            return 0;
        }
        String correctAnswer = question.getAnswer();
        if (correctAnswer == null || correctAnswer.trim().isEmpty()) {
            return 0;
        }

        int type = question.getType();
        int score = question.getScore();

        switch (type) {
            case Constants.TYPE_SINGLE:
            case Constants.TYPE_JUDGE:
                if (userAnswer.trim().equals(correctAnswer.trim())) {
                    return score;
                }
                return 0;

            case Constants.TYPE_MULTI:
                String sortedUser = userAnswer.chars().sorted()
                        .mapToObj(c -> String.valueOf((char) c))
                        .collect(Collectors.joining());
                String sortedCorrect = correctAnswer.chars().sorted()
                        .mapToObj(c -> String.valueOf((char) c))
                        .collect(Collectors.joining());
                if (sortedUser.equals(sortedCorrect)) {
                    return score;
                }
                return 0;

            case Constants.TYPE_FILL:
                if (userAnswer.trim().equals(correctAnswer.trim())) {
                    return score;
                }
                return 0;

            case Constants.TYPE_ESSAY:
                return 0;

            default:
                return 0;
        }
    }

    private ExamRecordVO buildRecordVO(ExamRecord record) {
        ExamRecordVO vo = new ExamRecordVO();
        BeanUtils.copyProperties(record, vo);
        return vo;
    }

    private void fillBatchInfo(List<ExamRecordVO> records) {
        if (records.isEmpty()) {
            return;
        }

        List<Long> examIds = records.stream().map(ExamRecordVO::getExamId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, Exam> examMap = new HashMap<>();
        if (!examIds.isEmpty()) {
            examMapper.selectBatchIds(examIds).forEach(e -> examMap.put(e.getId(), e));
        }

        List<Long> userIds = records.stream().map(ExamRecordVO::getUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMapper.selectBatchIds(userIds).forEach(u -> userMap.put(u.getId(), u));
        }

        List<Long> paperIds = records.stream().map(ExamRecordVO::getPaperId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, Paper> paperMap = new HashMap<>();
        if (!paperIds.isEmpty()) {
            paperMapper.selectBatchIds(paperIds).forEach(p -> paperMap.put(p.getId(), p));
        }

        for (ExamRecordVO vo : records) {
            Exam exam = examMap.get(vo.getExamId());
            if (exam != null) {
                vo.setExamName(exam.getName());
            }
            User user = userMap.get(vo.getUserId());
            if (user != null) {
                vo.setUserName(user.getUsername());
                vo.setRealName(user.getRealName());
            }
            Paper paper = paperMap.get(vo.getPaperId());
            if (paper != null) {
                vo.setTotalScore(paper.getTotalScore());
            }
        }
    }
}
