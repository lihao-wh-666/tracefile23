package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.dto.QuestionAnalysisDTO;
import com.exam.entity.*;
import com.exam.mapper.*;
import com.exam.service.QuestionAnalysisService;
import com.exam.vo.HighFrequencyWrongQuestionVO;
import com.exam.vo.QuestionAnalysisReportVO;
import com.exam.vo.QuestionAnalysisVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionAnalysisServiceImpl implements QuestionAnalysisService {

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private PaperQuestionMapper paperQuestionMapper;

    @Autowired
    private ExamAnswerMapper examAnswerMapper;

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private PaperMapper paperMapper;

    private static final Map<Integer, String> TYPE_MAP = new HashMap<>();
    private static final Map<Integer, String> DIFFICULTY_MAP = new HashMap<>();

    static {
        TYPE_MAP.put(1, "单选题");
        TYPE_MAP.put(2, "多选题");
        TYPE_MAP.put(3, "判断题");
        TYPE_MAP.put(4, "填空题");
        TYPE_MAP.put(5, "问答题");
        DIFFICULTY_MAP.put(1, "简单");
        DIFFICULTY_MAP.put(2, "中等");
        DIFFICULTY_MAP.put(3, "困难");
    }

    @Override
    public QuestionAnalysisReportVO generateAnalysisReport(QuestionAnalysisDTO dto) {
        QuestionAnalysisReportVO report = new QuestionAnalysisReportVO();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<QuestionAnalysisVO> analysisList = getQuestionAnalysisList(dto);

        if (analysisList.isEmpty()) {
            report.setTotalQuestionCount(0);
            report.setTotalAnswerCount(0);
            report.setTotalUserCount(0);
            report.setOverallCorrectRate(BigDecimal.ZERO);
            report.setOverallAverageScore(BigDecimal.ZERO);
            report.setQuestionAnalysisList(Collections.emptyList());
            report.setHighFrequencyWrongQuestions(Collections.emptyList());
            report.setAnalysisByType(Collections.emptyMap());
            report.setAnalysisByDifficulty(Collections.emptyMap());
            report.setOptimizationSuggestions(Collections.emptyList());
            report.setGenerateTime(LocalDateTime.now().format(formatter));
            return report;
        }

        if (dto.getSubjectId() != null) {
            Subject subject = subjectMapper.selectById(dto.getSubjectId());
            if (subject != null) {
                report.setSubjectId(subject.getId());
                report.setSubjectName(subject.getName());
            }
        }

        if (dto.getPaperId() != null) {
            Paper paper = paperMapper.selectById(dto.getPaperId());
            if (paper != null) {
                report.setPaperId(paper.getId());
                report.setPaperName(paper.getName());
            }
        }

        report.setTotalQuestionCount(analysisList.size());

        int totalAnswerCount = analysisList.stream().mapToInt(QuestionAnalysisVO::getTotalAnswerCount).sum();
        report.setTotalAnswerCount(totalAnswerCount);

        Set<Long> userIds = new HashSet<>();
        for (QuestionAnalysisVO vo : analysisList) {
            List<ExamAnswer> answers = examAnswerMapper.selectList(
                    new LambdaQueryWrapper<ExamAnswer>().eq(ExamAnswer::getQuestionId, vo.getQuestionId()));
            for (ExamAnswer answer : answers) {
                ExamRecord record = examRecordMapper.selectById(answer.getRecordId());
                if (record != null) {
                    userIds.add(record.getUserId());
                }
            }
        }
        report.setTotalUserCount(userIds.size());

        int totalCorrect = analysisList.stream().mapToInt(QuestionAnalysisVO::getCorrectCount).sum();
        BigDecimal overallCorrectRate = totalAnswerCount > 0
                ? BigDecimal.valueOf(totalCorrect).divide(BigDecimal.valueOf(totalAnswerCount), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        report.setOverallCorrectRate(overallCorrectRate);

        BigDecimal totalScoreSum = BigDecimal.valueOf(analysisList.stream().mapToInt(QuestionAnalysisVO::getTotalScoreSum).sum());
        int totalAnswerUsers = analysisList.stream().mapToInt(QuestionAnalysisVO::getAnswerUserCount).sum();
        BigDecimal overallAverageScore = totalAnswerUsers > 0
                ? totalScoreSum.divide(BigDecimal.valueOf(totalAnswerUsers), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        report.setOverallAverageScore(overallAverageScore);

        report.setQuestionAnalysisList(analysisList);

        List<HighFrequencyWrongQuestionVO> highFrequencyWrongQuestions = identifyHighFrequencyWrongQuestions(analysisList, dto);
        report.setHighFrequencyWrongQuestions(highFrequencyWrongQuestions);

        Map<String, List<QuestionAnalysisVO>> analysisByType = analysisList.stream()
                .collect(Collectors.groupingBy(QuestionAnalysisVO::getTypeName));
        report.setAnalysisByType(analysisByType);

        Map<String, List<QuestionAnalysisVO>> analysisByDifficulty = analysisList.stream()
                .collect(Collectors.groupingBy(QuestionAnalysisVO::getDifficultyName));
        report.setAnalysisByDifficulty(analysisByDifficulty);

        List<String> suggestions = generateOptimizationSuggestions(analysisList, highFrequencyWrongQuestions, overallCorrectRate);
        report.setOptimizationSuggestions(suggestions);

        report.setGenerateTime(LocalDateTime.now().format(formatter));

        return report;
    }

    @Override
    public List<QuestionAnalysisVO> getQuestionAnalysisList(QuestionAnalysisDTO dto) {
        List<Question> questions = getQuestionsByCondition(dto);
        if (questions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> questionIds = questions.stream().map(Question::getId).collect(Collectors.toList());

        Map<Long, Subject> subjectMap = subjectMapper.selectBatchIds(
                questions.stream().map(Question::getSubjectId).distinct().collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(Subject::getId, s -> s));

        List<QuestionAnalysisVO> result = new ArrayList<>();

        for (Question question : questions) {
            QuestionAnalysisVO vo = new QuestionAnalysisVO();
            BeanUtils.copyProperties(question, vo);
            vo.setQuestionId(question.getId());
            vo.setTypeName(TYPE_MAP.getOrDefault(question.getType(), "未知"));
            vo.setDifficultyName(DIFFICULTY_MAP.getOrDefault(question.getDifficulty(), "未知"));

            Subject subject = subjectMap.get(question.getSubjectId());
            if (subject != null) {
                vo.setSubjectName(subject.getName());
            }

            List<ExamAnswer> answers = examAnswerMapper.selectList(
                    new LambdaQueryWrapper<ExamAnswer>().eq(ExamAnswer::getQuestionId, question.getId()));

            int totalAnswerCount = answers.size();
            int correctCount = (int) answers.stream().filter(a -> a.getIsCorrect() != null && a.getIsCorrect() == 1).count();
            int wrongCount = totalAnswerCount - correctCount;
            int totalScoreSum = answers.stream().mapToInt(a -> a.getScore() != null ? a.getScore() : 0).sum();

            Set<Long> answerUserIds = new HashSet<>();
            for (ExamAnswer answer : answers) {
                ExamRecord record = examRecordMapper.selectById(answer.getRecordId());
                if (record != null) {
                    answerUserIds.add(record.getUserId());
                }
            }
            int answerUserCount = answerUserIds.size();

            vo.setTotalAnswerCount(totalAnswerCount);
            vo.setCorrectCount(correctCount);
            vo.setWrongCount(wrongCount);
            vo.setTotalScoreSum(totalScoreSum);
            vo.setAnswerUserCount(answerUserCount);

            BigDecimal correctRate = totalAnswerCount > 0
                    ? BigDecimal.valueOf(correctCount).divide(BigDecimal.valueOf(totalAnswerCount), 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal wrongRate = totalAnswerCount > 0
                    ? BigDecimal.valueOf(wrongCount).divide(BigDecimal.valueOf(totalAnswerCount), 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            BigDecimal averageScore = answerUserCount > 0
                    ? BigDecimal.valueOf(totalScoreSum).divide(BigDecimal.valueOf(answerUserCount), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            vo.setCorrectRate(correctRate);
            vo.setWrongRate(wrongRate);
            vo.setAverageScore(averageScore);

            result.add(vo);
        }

        return result;
    }

    private List<Question> getQuestionsByCondition(QuestionAnalysisDTO dto) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();

        if (dto.getPaperId() != null) {
            List<PaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                    new LambdaQueryWrapper<PaperQuestion>().eq(PaperQuestion::getPaperId, dto.getPaperId()));
            if (!paperQuestions.isEmpty()) {
                List<Long> questionIds = paperQuestions.stream()
                        .map(PaperQuestion::getQuestionId)
                        .collect(Collectors.toList());
                queryWrapper.in(Question::getId, questionIds);
            } else {
                return Collections.emptyList();
            }
        } else if (dto.getSubjectId() != null) {
            queryWrapper.eq(Question::getSubjectId, dto.getSubjectId());
        }

        return questionMapper.selectList(queryWrapper);
    }

    private List<HighFrequencyWrongQuestionVO> identifyHighFrequencyWrongQuestions(
            List<QuestionAnalysisVO> analysisList, QuestionAnalysisDTO dto) {

        BigDecimal threshold = dto.getWrongRateThreshold() != null
                ? dto.getWrongRateThreshold()
                : new BigDecimal("0.60");
        int topN = dto.getTopN() != null ? dto.getTopN() : 10;

        List<QuestionAnalysisVO> sortedByWrongRate = analysisList.stream()
                .sorted((a, b) -> b.getWrongRate().compareTo(a.getWrongRate()))
                .collect(Collectors.toList());

        Set<Long> addedQuestionIds = new HashSet<>();
        List<HighFrequencyWrongQuestionVO> result = new ArrayList<>();

        for (int i = 0; i < sortedByWrongRate.size(); i++) {
            QuestionAnalysisVO vo = sortedByWrongRate.get(i);
            if (addedQuestionIds.contains(vo.getQuestionId())) continue;

            boolean isHighFrequency = vo.getWrongRate().compareTo(threshold) >= 0 || i < topN;

            if (isHighFrequency && vo.getTotalAnswerCount() > 0) {
                HighFrequencyWrongQuestionVO hfvo = new HighFrequencyWrongQuestionVO();
                BeanUtils.copyProperties(vo, hfvo);
                hfvo.setQuestionId(vo.getQuestionId());
                hfvo.setWrongRank(i + 1);
                hfvo.setWrongReason(generateWrongReason(vo));
                result.add(hfvo);
                addedQuestionIds.add(vo.getQuestionId());
            }
        }

        return result;
    }

    private String generateWrongReason(QuestionAnalysisVO vo) {
        BigDecimal wrongRate = vo.getWrongRate();
        int difficulty = vo.getDifficulty();
        int type = vo.getType();

        if (wrongRate.compareTo(new BigDecimal("0.80")) >= 0) {
            return "错误率极高，题目可能存在歧义或难度过高，建议重新审核题目表述和答案准确性";
        } else if (wrongRate.compareTo(new BigDecimal("0.60")) >= 0) {
            if (difficulty == 3) {
                return "高难度题目错误率偏高，建议在教学中加强相关知识点的讲解和练习";
            } else if (type == 2) {
                return "多选题错误率较高，学生对知识点的全面掌握不足，建议加强辨析训练";
            } else {
                return "错误率较高，说明该知识点掌握不牢固，建议重点复习和强化训练";
            }
        } else if (wrongRate.compareTo(new BigDecimal("0.40")) >= 0) {
            return "错误率中等偏高，建议关注学生对该知识点的理解程度";
        } else {
            return "错误率在正常范围内，可作为常规知识点进行巩固";
        }
    }

    private List<String> generateOptimizationSuggestions(
            List<QuestionAnalysisVO> analysisList,
            List<HighFrequencyWrongQuestionVO> highFrequencyWrongQuestions,
            BigDecimal overallCorrectRate) {

        List<String> suggestions = new ArrayList<>();

        if (overallCorrectRate.compareTo(new BigDecimal("0.60")) < 0) {
            suggestions.add("整体正确率偏低（" + overallCorrectRate.multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP) + "%），建议全面评估教学质量和学生学习效果，考虑调整教学方法和课程难度。");
        } else if (overallCorrectRate.compareTo(new BigDecimal("0.80")) < 0) {
            suggestions.add("整体正确率中等（" + overallCorrectRate.multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP) + "%），建议针对薄弱环节进行重点强化教学。");
        } else {
            suggestions.add("整体正确率良好（" + overallCorrectRate.multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP) + "%），建议保持现有教学方法，同时关注个别学生的学习情况。");
        }

        if (!highFrequencyWrongQuestions.isEmpty()) {
            suggestions.add("发现 " + highFrequencyWrongQuestions.size() + " 道高频错题，建议：");
            suggestions.add("  1. 对高频错题涉及的知识点进行专项讲解和强化训练");
            suggestions.add("  2. 将高频错题整理成错题集，供学生反复练习");
            suggestions.add("  3. 分析错误原因，如是题目本身问题，及时优化题目表述和选项设置");
        }

        Map<Integer, List<QuestionAnalysisVO>> typeGroup = analysisList.stream()
                .collect(Collectors.groupingBy(QuestionAnalysisVO::getType));

        for (Map.Entry<Integer, List<QuestionAnalysisVO>> entry : typeGroup.entrySet()) {
            int type = entry.getKey();
            List<QuestionAnalysisVO> list = entry.getValue();
            BigDecimal avgCorrectRate = list.stream()
                    .map(QuestionAnalysisVO::getCorrectRate)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(list.size()), 4, RoundingMode.HALF_UP);

            String typeName = TYPE_MAP.getOrDefault(type, "未知");
            if (avgCorrectRate.compareTo(new BigDecimal("0.50")) < 0) {
                suggestions.add(typeName + "平均正确率仅为 " + avgCorrectRate.multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP) + "%，建议加强该题型的解题技巧训练。");
            }
        }

        Map<Integer, List<QuestionAnalysisVO>> difficultyGroup = analysisList.stream()
                .collect(Collectors.groupingBy(QuestionAnalysisVO::getDifficulty));

        for (Map.Entry<Integer, List<QuestionAnalysisVO>> entry : difficultyGroup.entrySet()) {
            int difficulty = entry.getKey();
            List<QuestionAnalysisVO> list = entry.getValue();
            BigDecimal avgCorrectRate = list.stream()
                    .map(QuestionAnalysisVO::getCorrectRate)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(list.size()), 4, RoundingMode.HALF_UP);

            String diffName = DIFFICULTY_MAP.getOrDefault(difficulty, "未知");
            suggestions.add(diffName + "题目平均正确率：" + avgCorrectRate.multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP) + "%（共" + list.size() + "题）");
        }

        long noAnswerCount = analysisList.stream().filter(q -> q.getTotalAnswerCount() == 0).count();
        if (noAnswerCount > 0) {
            suggestions.add("有 " + noAnswerCount + " 道题目尚未有学生作答，建议在后续考试中安排这些题目以收集更多数据。");
        }

        return suggestions;
    }
}
