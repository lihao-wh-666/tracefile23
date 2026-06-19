package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Constants;
import com.exam.entity.*;
import com.exam.mapper.*;
import com.exam.service.impl.ExamRecordServiceImpl;
import com.exam.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("个人成绩台账模块单元测试")
class PersonalScoreServiceTest {

    @Mock
    private ExamRecordMapper examRecordMapper;

    @Mock
    private ExamMapper examMapper;

    @Mock
    private PaperMapper paperMapper;

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private PaperQuestionMapper paperQuestionMapper;

    @Mock
    private ExamAnswerMapper examAnswerMapper;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private ExamRecordServiceImpl examRecordService;

    private Exam testExam;
    private Paper testPaper;
    private User testUser;
    private List<ExamRecord> testExamRecords;
    private List<ExamAnswer> testExamAnswers;
    private List<Question> testQuestions;

    @BeforeEach
    void setUp() {
        testPaper = new Paper();
        testPaper.setId(1L);
        testPaper.setName("Java基础试卷");
        testPaper.setTotalScore(100);
        testPaper.setPassScore(60);
        testPaper.setDuration(90);

        testExam = new Exam();
        testExam.setId(1L);
        testExam.setPaperId(1L);
        testExam.setName("Java期中考试");
        testExam.setStartTime(LocalDateTime.now().minusDays(1));
        testExam.setEndTime(LocalDateTime.now().minusDays(1).plusHours(2));

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRealName("测试用户");
        testUser.setRole(Constants.ROLE_STUDENT);

        testExamRecords = new ArrayList<>();
        ExamRecord record1 = new ExamRecord();
        record1.setId(1L);
        record1.setExamId(1L);
        record1.setUserId(1L);
        record1.setPaperId(1L);
        record1.setStartTime(LocalDateTime.now().minusDays(1));
        record1.setSubmitTime(LocalDateTime.now().minusDays(1).plusMinutes(90));
        record1.setScore(85);
        record1.setStatus(Constants.RECORD_SUBMITTED);
        record1.setDuration(5400);
        testExamRecords.add(record1);

        ExamRecord record2 = new ExamRecord();
        record2.setId(2L);
        record2.setExamId(2L);
        record2.setUserId(1L);
        record2.setPaperId(1L);
        record2.setStartTime(LocalDateTime.now().minusDays(2));
        record2.setSubmitTime(LocalDateTime.now().minusDays(2).plusMinutes(80));
        record2.setScore(70);
        record2.setStatus(Constants.RECORD_GRADED);
        record2.setDuration(4800);
        testExamRecords.add(record2);

        ExamRecord record3 = new ExamRecord();
        record3.setId(3L);
        record3.setExamId(3L);
        record3.setUserId(1L);
        record3.setPaperId(1L);
        record3.setStartTime(LocalDateTime.now().minusDays(3));
        record3.setSubmitTime(LocalDateTime.now().minusDays(3).plusMinutes(85));
        record3.setScore(95);
        record3.setStatus(Constants.RECORD_SUBMITTED);
        record3.setDuration(5100);
        testExamRecords.add(record3);

        testExamAnswers = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            ExamAnswer ea = new ExamAnswer();
            ea.setId(i);
            ea.setRecordId(1L);
            ea.setQuestionId(i);
            ea.setAnswer("A");
            ea.setIsCorrect(1);
            ea.setScore(10);
            ea.setAutoScore(10);
            testExamAnswers.add(ea);
        }

        ExamAnswer wrongAnswer = new ExamAnswer();
        wrongAnswer.setId(4L);
        wrongAnswer.setRecordId(1L);
        wrongAnswer.setQuestionId(4L);
        wrongAnswer.setAnswer("B");
        wrongAnswer.setIsCorrect(0);
        wrongAnswer.setScore(0);
        wrongAnswer.setAutoScore(0);
        testExamAnswers.add(wrongAnswer);

        ExamAnswer essayAnswer = new ExamAnswer();
        essayAnswer.setId(5L);
        essayAnswer.setRecordId(1L);
        essayAnswer.setQuestionId(5L);
        essayAnswer.setAnswer("我的答案");
        essayAnswer.setIsCorrect(2);
        essayAnswer.setScore(0);
        essayAnswer.setAutoScore(0);
        testExamAnswers.add(essayAnswer);

        testQuestions = new ArrayList<>();
        for (long i = 1; i <= 5; i++) {
            Question q = new Question();
            q.setId(i);
            q.setType(i <= 3 ? Constants.TYPE_SINGLE : (i == 4 ? Constants.TYPE_SINGLE : Constants.TYPE_ESSAY));
            q.setContent("题目" + i);
            q.setOptionA("选项A");
            q.setOptionB("选项B");
            q.setOptionC("选项C");
            q.setOptionD("选项D");
            q.setAnswer("A");
            q.setScore(10);
            testQuestions.add(q);
        }
    }

    @Test
    @DisplayName("获取个人成绩统计 - 正常情况")
    void testGetPersonalStat_Normal() {
        when(examRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamRecords);
        when(examMapper.selectById(3L)).thenReturn(testExam);
        when(examAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamAnswers);
        when(paperMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testPaper));

        PersonalScoreStatVO result = examRecordService.getPersonalStat(1L);

        assertNotNull(result);
        assertEquals(3, result.getTotalExamCount());
        assertEquals(3, result.getSubmittedCount());
        assertEquals(95, result.getMaxScore());
        assertNotNull(result.getAvgScore());
        assertTrue(result.getAvgScore() > 0);
        assertNotNull(result.getAccuracyRate());
        verify(examRecordMapper).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取个人成绩统计 - 无考试记录")
    void testGetPersonalStat_NoRecords() {
        when(examRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

        PersonalScoreStatVO result = examRecordService.getPersonalStat(999L);

        assertNotNull(result);
        assertEquals(0, result.getTotalExamCount());
        assertEquals(0, result.getSubmittedCount());
        assertEquals(0, result.getMaxScore());
        assertEquals(0.0, result.getAvgScore());
        assertEquals(0.0, result.getAccuracyRate());
        assertEquals(0, result.getTotalQuestionCount());
        assertEquals(0, result.getCorrectQuestionCount());
        assertEquals(0, result.getWrongQuestionCount());
        assertEquals(0, result.getTotalScore());
        assertEquals(0, result.getTotalFullScore());
        verify(examRecordMapper).selectList(any(LambdaQueryWrapper.class));
        verify(examAnswerMapper, never()).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取个人成绩统计 - 最高分考试名称")
    void testGetPersonalStat_MaxScoreExamName() {
        when(examRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamRecords);

        Exam exam3 = new Exam();
        exam3.setId(3L);
        exam3.setName("Java期末考试");
        when(examMapper.selectById(3L)).thenReturn(exam3);

        when(examAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamAnswers);
        when(paperMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testPaper));

        PersonalScoreStatVO result = examRecordService.getPersonalStat(1L);

        assertNotNull(result);
        assertEquals(95, result.getMaxScore());
        assertEquals("Java期末考试", result.getMaxScoreExamName());
    }

    @Test
    @DisplayName("获取个人成绩统计 - 正确率计算")
    void testGetPersonalStat_AccuracyRate() {
        when(examRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamRecords.subList(0, 1));
        when(examAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamAnswers);
        when(paperMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testPaper));

        PersonalScoreStatVO result = examRecordService.getPersonalStat(1L);

        assertNotNull(result);
        assertEquals(5, result.getTotalQuestionCount());
        assertEquals(3, result.getCorrectQuestionCount());
        assertEquals(1, result.getWrongQuestionCount());
        assertNotNull(result.getAccuracyRate());
        assertTrue(result.getAccuracyRate() > 0);
    }

    @Test
    @DisplayName("获取个人成绩统计 - 总分计算")
    void testGetPersonalStat_TotalScore() {
        when(examRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamRecords);
        when(examMapper.selectById(anyLong())).thenReturn(testExam);
        when(examAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamAnswers);
        when(paperMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testPaper));

        PersonalScoreStatVO result = examRecordService.getPersonalStat(1L);

        assertNotNull(result);
        assertEquals(250, result.getTotalScore());
        assertEquals(300, result.getTotalFullScore());
    }

    @Test
    @DisplayName("获取我的考试记录 - 正常情况")
    void testGetMyRecords_Normal() {
        Page<ExamRecord> recordPage = new Page<>(1, 10);
        recordPage.setRecords(testExamRecords);
        recordPage.setTotal(3);

        when(examRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(recordPage);
        when(examMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testExam));
        when(userMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testUser));
        when(paperMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testPaper));

        IPage<ExamRecordVO> result = examRecordService.getMyRecords(1, 10, 1L);

        assertNotNull(result);
        assertEquals(3, result.getTotal());
        assertEquals(3, result.getRecords().size());
        assertEquals("Java期中考试", result.getRecords().get(0).getExamName());
        assertEquals("测试用户", result.getRecords().get(0).getRealName());
        verify(examRecordMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取我的考试记录 - 无记录")
    void testGetMyRecords_Empty() {
        Page<ExamRecord> recordPage = new Page<>(1, 10);
        recordPage.setRecords(new ArrayList<>());
        recordPage.setTotal(0);

        when(examRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(recordPage);

        IPage<ExamRecordVO> result = examRecordService.getMyRecords(1, 10, 999L);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    @DisplayName("获取我的考试记录 - 按提交时间降序排序")
    void testGetMyRecords_OrderBySubmitTimeDesc() {
        Page<ExamRecord> recordPage = new Page<>(1, 10);
        recordPage.setRecords(new ArrayList<>());
        recordPage.setTotal(0);

        when(examRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenAnswer(invocation -> {
                    Page<ExamRecord> page = invocation.getArgument(0);
                    assertNotNull(page);
                    return recordPage;
                });

        examRecordService.getMyRecords(1, 10, 1L);

        verify(examRecordMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取错题列表 - 正常情况")
    void testGetWrongQuestions_Normal() {
        List<ExamAnswer> wrongAnswers = new ArrayList<>();
        ExamAnswer wrongAnswer = new ExamAnswer();
        wrongAnswer.setId(1L);
        wrongAnswer.setRecordId(1L);
        wrongAnswer.setQuestionId(1L);
        wrongAnswer.setAnswer("B");
        wrongAnswer.setIsCorrect(0);
        wrongAnswer.setScore(0);
        wrongAnswers.add(wrongAnswer);

        when(examRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamRecords.subList(0, 1));
        when(examAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(wrongAnswers);
        when(questionMapper.selectBatchIds(anyList())).thenReturn(testQuestions.subList(0, 1));
        when(examMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testExam));

        IPage<WrongQuestionVO> result = examRecordService.getWrongQuestions(1, 10, 1L);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("B", result.getRecords().get(0).getUserAnswer());
        assertEquals("A", result.getRecords().get(0).getCorrectAnswer());
    }

    @Test
    @DisplayName("获取错题列表 - 无考试记录")
    void testGetWrongQuestions_NoRecords() {
        when(examRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

        IPage<WrongQuestionVO> result = examRecordService.getWrongQuestions(1, 10, 999L);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
        verify(examAnswerMapper, never()).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("获取错题列表 - 没有错题")
    void testGetWrongQuestions_NoWrongQuestions() {
        when(examRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamRecords.subList(0, 1));
        when(examAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

        IPage<WrongQuestionVO> result = examRecordService.getWrongQuestions(1, 10, 1L);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
    }

    @Test
    @DisplayName("获取错题列表 - 分页")
    void testGetWrongQuestions_Pagination() {
        List<ExamAnswer> wrongAnswers = new ArrayList<>();
        for (long i = 1; i <= 5; i++) {
            ExamAnswer ea = new ExamAnswer();
            ea.setId(i);
            ea.setRecordId(1L);
            ea.setQuestionId(i);
            ea.setAnswer("B");
            ea.setIsCorrect(0);
            ea.setScore(0);
            wrongAnswers.add(ea);
        }

        when(examRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamRecords.subList(0, 1));
        when(examAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(wrongAnswers);
        when(questionMapper.selectBatchIds(anyList())).thenReturn(testQuestions);
        when(examMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testExam));

        IPage<WrongQuestionVO> result = examRecordService.getWrongQuestions(1, 3, 1L);

        assertNotNull(result);
        assertEquals(5, result.getTotal());
        assertEquals(3, result.getRecords().size());
    }

    @Test
    @DisplayName("成绩统计 - 正常情况")
    void testScoreStats_Normal() {
        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(paperMapper.selectById(1L)).thenReturn(testPaper);
        when(examRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamRecords);

        List<ScoreStatVO> result = examRecordService.scoreStats(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        ScoreStatVO stat = result.get(0);
        assertEquals(1L, stat.getExamId());
        assertEquals("Java期中考试", stat.getExamName());
        assertEquals(3, stat.getTotalCount());
        assertEquals(83.33, stat.getAvgScore());
        assertEquals(95, stat.getMaxScore());
        assertEquals(70, stat.getMinScore());
        assertNotNull(stat.getPassCount());
        assertNotNull(stat.getPassRate());
    }

    @Test
    @DisplayName("成绩统计 - 考试不存在")
    void testScoreStats_ExamNotFound() {
        when(examMapper.selectById(999L)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            examRecordService.scoreStats(999L);
        });

        assertEquals("考试不存在", exception.getMessage());
    }

    @Test
    @DisplayName("成绩统计 - 无考试记录")
    void testScoreStats_NoRecords() {
        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(paperMapper.selectById(1L)).thenReturn(testPaper);
        when(examRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(new ArrayList<>());

        List<ScoreStatVO> result = examRecordService.scoreStats(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        ScoreStatVO stat = result.get(0);
        assertEquals(0, stat.getTotalCount());
        assertEquals(0.0, stat.getAvgScore());
        assertEquals(0, stat.getMaxScore());
        assertEquals(0, stat.getMinScore());
        assertEquals(0, stat.getPassCount());
        assertEquals(0.0, stat.getPassRate());
    }

    @Test
    @DisplayName("成绩统计 - 全部及格")
    void testScoreStats_AllPass() {
        List<ExamRecord> records = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            ExamRecord record = new ExamRecord();
            record.setId(i);
            record.setExamId(1L);
            record.setScore(70 + (int) i * 10);
            record.setStatus(Constants.RECORD_SUBMITTED);
            records.add(record);
        }

        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(paperMapper.selectById(1L)).thenReturn(testPaper);
        when(examRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(records);

        List<ScoreStatVO> result = examRecordService.scoreStats(1L);

        assertNotNull(result);
        assertEquals(3, result.get(0).getPassCount());
        assertEquals(100.0, result.get(0).getPassRate());
    }

    @Test
    @DisplayName("成绩统计 - 全部不及格")
    void testScoreStats_AllFail() {
        List<ExamRecord> records = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            ExamRecord record = new ExamRecord();
            record.setId(i);
            record.setExamId(1L);
            record.setScore(30 + (int) i * 5);
            record.setStatus(Constants.RECORD_SUBMITTED);
            records.add(record);
        }

        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(paperMapper.selectById(1L)).thenReturn(testPaper);
        when(examRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(records);

        List<ScoreStatVO> result = examRecordService.scoreStats(1L);

        assertNotNull(result);
        assertEquals(0, result.get(0).getPassCount());
        assertEquals(0.0, result.get(0).getPassRate());
    }

    @Test
    @DisplayName("分页查询考试记录 - 正常情况")
    void testPage_Normal() {
        Page<ExamRecord> recordPage = new Page<>(1, 10);
        recordPage.setRecords(testExamRecords);
        recordPage.setTotal(3);

        when(examRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(recordPage);
        when(examMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testExam));
        when(userMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testUser));
        when(paperMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testPaper));

        IPage<ExamRecordVO> result = examRecordService.page(1, 10, 1L, 1L, Constants.RECORD_SUBMITTED);

        assertNotNull(result);
        assertEquals(3, result.getTotal());
        verify(examRecordMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("分页查询考试记录 - 无筛选条件")
    void testPage_NoFilters() {
        Page<ExamRecord> recordPage = new Page<>(1, 10);
        recordPage.setRecords(testExamRecords);
        recordPage.setTotal(3);

        when(examRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(recordPage);
        when(examMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testExam));
        when(userMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testUser));
        when(paperMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testPaper));

        IPage<ExamRecordVO> result = examRecordService.page(1, 10, null, null, null);

        assertNotNull(result);
        assertEquals(3, result.getTotal());
    }

    @Test
    @DisplayName("获取考试记录详情 - 正常情况")
    void testGetDetail_Normal() {
        when(examRecordMapper.selectById(1L)).thenReturn(testExamRecords.get(0));
        when(examMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testExam));
        when(userMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testUser));
        when(paperMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testPaper));

        ExamRecordVO result = examRecordService.getDetail(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Java期中考试", result.getExamName());
        assertEquals("测试用户", result.getRealName());
        assertEquals(100, result.getTotalScore());
    }

    @Test
    @DisplayName("获取考试记录详情 - 记录不存在")
    void testGetDetail_NotFound() {
        when(examRecordMapper.selectById(999L)).thenReturn(null);

        ExamRecordVO result = examRecordService.getDetail(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("状态转换验证 - 已提交和已阅卷状态都计入统计")
    void testStatusTransition_SubmittedAndGradedCounted() {
        List<ExamRecord> records = new ArrayList<>();

        ExamRecord submitted = new ExamRecord();
        submitted.setId(1L);
        submitted.setExamId(1L);
        submitted.setUserId(1L);
        submitted.setScore(80);
        submitted.setStatus(Constants.RECORD_SUBMITTED);
        records.add(submitted);

        ExamRecord graded = new ExamRecord();
        graded.setId(2L);
        graded.setExamId(1L);
        graded.setUserId(2L);
        graded.setScore(90);
        graded.setStatus(Constants.RECORD_GRADED);
        records.add(graded);

        ExamRecord paused = new ExamRecord();
        paused.setId(3L);
        paused.setExamId(1L);
        paused.setUserId(3L);
        paused.setScore(0);
        paused.setStatus(Constants.RECORD_PAUSED);
        records.add(paused);

        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(paperMapper.selectById(1L)).thenReturn(testPaper);
        when(examRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(records.subList(0, 2));

        List<ScoreStatVO> result = examRecordService.scoreStats(1L);

        assertNotNull(result);
        assertEquals(2, result.get(0).getTotalCount());
    }

    @Test
    @DisplayName("边界测试 - 只有一道题的统计")
    void testBoundary_SingleQuestion() {
        List<ExamAnswer> singleAnswer = new ArrayList<>();
        ExamAnswer ea = new ExamAnswer();
        ea.setId(1L);
        ea.setRecordId(1L);
        ea.setQuestionId(1L);
        ea.setAnswer("A");
        ea.setIsCorrect(1);
        ea.setScore(10);
        singleAnswer.add(ea);

        when(examRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamRecords.subList(0, 1));
        when(examAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(singleAnswer);
        when(examMapper.selectById(anyLong())).thenReturn(testExam);
        when(paperMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testPaper));

        PersonalScoreStatVO result = examRecordService.getPersonalStat(1L);

        assertNotNull(result);
        assertEquals(1, result.getTotalQuestionCount());
        assertEquals(1, result.getCorrectQuestionCount());
        assertEquals(0, result.getWrongQuestionCount());
        assertEquals(100.0, result.getAccuracyRate());
    }

    @Test
    @DisplayName("边界测试 - 零分考试")
    void testBoundary_ZeroScore() {
        List<ExamRecord> records = new ArrayList<>();
        ExamRecord record = new ExamRecord();
        record.setId(1L);
        record.setExamId(1L);
        record.setUserId(1L);
        record.setPaperId(1L);
        record.setScore(0);
        record.setStatus(Constants.RECORD_SUBMITTED);
        records.add(record);

        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(paperMapper.selectById(1L)).thenReturn(testPaper);
        when(examRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(records);

        List<ScoreStatVO> result = examRecordService.scoreStats(1L);

        assertNotNull(result);
        assertEquals(1, result.get(0).getTotalCount());
        assertEquals(0, result.get(0).getMinScore());
        assertEquals(0, result.get(0).getMaxScore());
        assertEquals(0.0, result.get(0).getAvgScore());
    }

    @Test
    @DisplayName("边界测试 - 满分考试")
    void testBoundary_FullScore() {
        List<ExamRecord> records = new ArrayList<>();
        ExamRecord record = new ExamRecord();
        record.setId(1L);
        record.setExamId(1L);
        record.setUserId(1L);
        record.setPaperId(1L);
        record.setScore(100);
        record.setStatus(Constants.RECORD_SUBMITTED);
        records.add(record);

        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(paperMapper.selectById(1L)).thenReturn(testPaper);
        when(examRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(records);

        List<ScoreStatVO> result = examRecordService.scoreStats(1L);

        assertNotNull(result);
        assertEquals(100, result.get(0).getMaxScore());
        assertEquals(100.0, result.get(0).getAvgScore());
    }

    @Test
    @DisplayName("接口交互测试 - 个人统计与多个Mapper交互")
    void testInterfaceInteraction_PersonalStat() {
        when(examRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamRecords);
        when(examMapper.selectById(anyLong())).thenReturn(testExam);
        when(examAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamAnswers);
        when(paperMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testPaper));

        examRecordService.getPersonalStat(1L);

        verify(examRecordMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        verify(examMapper, atLeast(1)).selectById(anyLong());
        verify(examAnswerMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        verify(paperMapper, times(1)).selectBatchIds(anyList());
    }

    @Test
    @DisplayName("接口交互测试 - 我的记录与多个Mapper交互")
    void testInterfaceInteraction_MyRecords() {
        Page<ExamRecord> recordPage = new Page<>(1, 10);
        recordPage.setRecords(testExamRecords);
        recordPage.setTotal(3);

        when(examRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(recordPage);
        when(examMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testExam));
        when(userMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testUser));
        when(paperMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testPaper));

        examRecordService.getMyRecords(1, 10, 1L);

        verify(examRecordMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        verify(examMapper, times(1)).selectBatchIds(anyList());
        verify(userMapper, times(1)).selectBatchIds(anyList());
        verify(paperMapper, times(1)).selectBatchIds(anyList());
    }

    @Test
    @DisplayName("输入数据校验 - 合法的分页参数")
    void testInputValidation_ValidPagination() {
        Page<ExamRecord> recordPage = new Page<>(1, 10);
        recordPage.setRecords(new ArrayList<>());
        recordPage.setTotal(0);

        when(examRecordMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(recordPage);

        IPage<ExamRecordVO> result = examRecordService.getMyRecords(1, 10, 1L);

        assertNotNull(result);
        assertEquals(1, result.getCurrent());
        assertEquals(10, result.getSize());
    }

    @Test
    @DisplayName("大量数据测试 - 100条考试记录统计")
    void testLargeData_100Records() {
        List<ExamRecord> records = new ArrayList<>();
        for (long i = 1; i <= 100; i++) {
            ExamRecord record = new ExamRecord();
            record.setId(i);
            record.setExamId(1L);
            record.setUserId(i);
            record.setPaperId(1L);
            record.setScore(50 + (int) (i % 51));
            record.setStatus(Constants.RECORD_SUBMITTED);
            records.add(record);
        }

        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(paperMapper.selectById(1L)).thenReturn(testPaper);
        when(examRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(records);

        List<ScoreStatVO> result = examRecordService.scoreStats(1L);

        assertNotNull(result);
        assertEquals(100, result.get(0).getTotalCount());
        assertTrue(result.get(0).getAvgScore() > 0);
    }
}
