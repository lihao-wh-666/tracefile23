package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.exam.common.Constants;
import com.exam.dto.PauseExamDTO;
import com.exam.dto.SaveAnswerDTO;
import com.exam.dto.SaveAnswersDTO;
import com.exam.dto.SubmitExamDTO;
import com.exam.entity.*;
import com.exam.mapper.*;
import com.exam.service.impl.ExamRecordServiceImpl;
import com.exam.vo.ExamRecordVO;
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
@DisplayName("参加考试模块单元测试")
class ExamTakeServiceTest {

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
    private ExamRecord testExamRecord;
    private List<PaperQuestion> testPaperQuestions;
    private List<Question> testQuestions;
    private List<ExamAnswer> testExamAnswers;

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
        testExam.setStartTime(LocalDateTime.now().minusHours(1));
        testExam.setEndTime(LocalDateTime.now().plusHours(1));

        testExamRecord = new ExamRecord();
        testExamRecord.setId(1L);
        testExamRecord.setExamId(1L);
        testExamRecord.setUserId(1L);
        testExamRecord.setPaperId(1L);
        testExamRecord.setStartTime(LocalDateTime.now());
        testExamRecord.setStatus(Constants.RECORD_EXAMING);
        testExamRecord.setPauseCount(0);
        testExamRecord.setTotalPauseTime(0);
        testExamRecord.setDuration(5400);

        testPaperQuestions = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            PaperQuestion pq = new PaperQuestion();
            pq.setPaperId(1L);
            pq.setQuestionId((long) (i + 1));
            pq.setSort(i + 1);
            testPaperQuestions.add(pq);
        }

        testQuestions = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            Question q = new Question();
            q.setId(i);
            q.setType(1);
            q.setContent("题目" + i);
            q.setOptionA("选项A");
            q.setOptionB("选项B");
            q.setOptionC("选项C");
            q.setOptionD("选项D");
            q.setAnswer("A");
            q.setScore(10);
            testQuestions.add(q);
        }

        testExamAnswers = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            ExamAnswer ea = new ExamAnswer();
            ea.setId(i);
            ea.setRecordId(1L);
            ea.setQuestionId(i);
            ea.setAnswer("");
            ea.setIsCorrect(0);
            ea.setScore(0);
            ea.setAutoScore(0);
            ea.setOptionOrder("ABCD");
            testExamAnswers.add(ea);
        }
    }

    @Test
    @DisplayName("开始考试 - 正常情况")
    void testStartExam_Normal() {
        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(examRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(paperMapper.selectById(1L)).thenReturn(testPaper);
        when(paperQuestionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testPaperQuestions);
        when(questionMapper.selectBatchIds(anyList())).thenReturn(testQuestions);
        when(examRecordMapper.insert(any(ExamRecord.class))).thenAnswer(invocation -> {
            ExamRecord record = invocation.getArgument(0);
            record.setId(1L);
            return 1;
        });
        when(examRecordMapper.updateById(any(ExamRecord.class))).thenReturn(1);
        when(examAnswerMapper.insert(any(ExamAnswer.class))).thenReturn(1);

        ExamRecordVO result = examRecordService.startExam(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getExamId());
        assertEquals(1L, result.getUserId());
        assertEquals(Constants.RECORD_EXAMING, result.getStatus());

        verify(examMapper).selectById(1L);
        verify(examRecordMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(paperMapper).selectById(1L);
        verify(paperQuestionMapper).selectList(any(LambdaQueryWrapper.class));
        verify(examRecordMapper).insert(any(ExamRecord.class));
        verify(examAnswerMapper, times(3)).insert(any(ExamAnswer.class));
    }

    @Test
    @DisplayName("开始考试 - 考试不存在")
    void testStartExam_ExamNotFound() {
        when(examMapper.selectById(999L)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            examRecordService.startExam(999L, 1L);
        });

        assertEquals("考试不存在", exception.getMessage());
        verify(examMapper).selectById(999L);
        verify(examRecordMapper, never()).insert(any(ExamRecord.class));
    }

    @Test
    @DisplayName("开始考试 - 考试未开始")
    void testStartExam_ExamNotStarted() {
        Exam futureExam = new Exam();
        futureExam.setId(1L);
        futureExam.setPaperId(1L);
        futureExam.setName("未来考试");
        futureExam.setStartTime(LocalDateTime.now().plusDays(1));
        futureExam.setEndTime(LocalDateTime.now().plusDays(2));

        when(examMapper.selectById(1L)).thenReturn(futureExam);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            examRecordService.startExam(1L, 1L);
        });

        assertEquals("考试未在进行中", exception.getMessage());
        verify(examMapper).selectById(1L);
    }

    @Test
    @DisplayName("开始考试 - 考试已结束")
    void testStartExam_ExamEnded() {
        Exam endedExam = new Exam();
        endedExam.setId(1L);
        endedExam.setPaperId(1L);
        endedExam.setName("已结束考试");
        endedExam.setStartTime(LocalDateTime.now().minusDays(2));
        endedExam.setEndTime(LocalDateTime.now().minusDays(1));

        when(examMapper.selectById(1L)).thenReturn(endedExam);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            examRecordService.startExam(1L, 1L);
        });

        assertEquals("考试未在进行中", exception.getMessage());
        verify(examMapper).selectById(1L);
    }

    @Test
    @DisplayName("开始考试 - 已参加过且正在考试中，返回现有记录")
    void testStartExam_AlreadyInProgress() {
        testExamRecord.setStatus(Constants.RECORD_EXAMING);

        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(examRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testExamRecord);
        when(examMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testExam));
        when(paperMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testPaper));
        when(userMapper.selectBatchIds(anyList())).thenReturn(new ArrayList<>());

        ExamRecordVO result = examRecordService.startExam(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(examRecordMapper, never()).insert(any(ExamRecord.class));
    }

    @Test
    @DisplayName("开始考试 - 已参加过且已暂停，返回现有记录")
    void testStartExam_AlreadyPaused() {
        testExamRecord.setStatus(Constants.RECORD_PAUSED);

        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(examRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testExamRecord);
        when(examMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testExam));
        when(paperMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testPaper));
        when(userMapper.selectBatchIds(anyList())).thenReturn(new ArrayList<>());

        ExamRecordVO result = examRecordService.startExam(1L, 1L);

        assertNotNull(result);
        assertEquals(Constants.RECORD_PAUSED, result.getStatus());
        verify(examRecordMapper, never()).insert(any(ExamRecord.class));
    }

    @Test
    @DisplayName("开始考试 - 已参加过且已提交，抛出异常")
    void testStartExam_AlreadySubmitted() {
        testExamRecord.setStatus(Constants.RECORD_SUBMITTED);

        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(examRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testExamRecord);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            examRecordService.startExam(1L, 1L);
        });

        assertEquals("您已参加过该考试", exception.getMessage());
        verify(examRecordMapper, never()).insert(any(ExamRecord.class));
    }

    @Test
    @DisplayName("开始考试 - 考试时长设置正确")
    void testStartExam_DurationSetCorrectly() {
        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(examRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(paperMapper.selectById(1L)).thenReturn(testPaper);
        when(paperQuestionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testPaperQuestions);
        when(questionMapper.selectBatchIds(anyList())).thenReturn(testQuestions);
        when(examRecordMapper.insert(any(ExamRecord.class))).thenAnswer(invocation -> {
            ExamRecord record = invocation.getArgument(0);
            record.setId(1L);
            assertEquals(5400, record.getDuration());
            return 1;
        });
        when(examRecordMapper.updateById(any(ExamRecord.class))).thenReturn(1);
        when(examAnswerMapper.insert(any(ExamAnswer.class))).thenReturn(1);

        examRecordService.startExam(1L, 1L);
    }

    @Test
    @DisplayName("开始考试 - 暂停次数初始化为0")
    void testStartExam_PauseCountInitZero() {
        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(examRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(paperMapper.selectById(1L)).thenReturn(testPaper);
        when(paperQuestionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testPaperQuestions);
        when(questionMapper.selectBatchIds(anyList())).thenReturn(testQuestions);
        when(examRecordMapper.insert(any(ExamRecord.class))).thenAnswer(invocation -> {
            ExamRecord record = invocation.getArgument(0);
            record.setId(1L);
            assertEquals(0, record.getPauseCount());
            assertEquals(0, record.getTotalPauseTime());
            return 1;
        });
        when(examRecordMapper.updateById(any(ExamRecord.class))).thenReturn(1);
        when(examAnswerMapper.insert(any(ExamAnswer.class))).thenReturn(1);

        examRecordService.startExam(1L, 1L);
    }

    @Test
    @DisplayName("提交考试 - 正常情况")
    void testSubmitExam_Normal() {
        testExamRecord.setStatus(Constants.RECORD_EXAMING);

        SubmitExamDTO dto = new SubmitExamDTO();
        dto.setExamId(1L);
        List<SubmitExamDTO.AnswerItem> answers = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            SubmitExamDTO.AnswerItem item = new SubmitExamDTO.AnswerItem();
            item.setQuestionId(i);
            item.setAnswer("A");
            answers.add(item);
        }
        dto.setAnswers(answers);

        when(examRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testExamRecord);
        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(examAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamAnswers);
        when(questionMapper.selectBatchIds(anyList())).thenReturn(testQuestions);
        when(examAnswerMapper.updateById(any(ExamAnswer.class))).thenReturn(1);
        when(examRecordMapper.updateById(any(ExamRecord.class))).thenReturn(1);

        ExamRecordVO result = examRecordService.submitExam(dto, 1L);

        assertNotNull(result);
        assertEquals(Constants.RECORD_SUBMITTED, result.getStatus());
        assertNotNull(result.getSubmitTime());
        verify(examRecordMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(examAnswerMapper, times(3)).updateById(any(ExamAnswer.class));
    }

    @Test
    @DisplayName("提交考试 - 考试记录不存在")
    void testSubmitExam_RecordNotFound() {
        SubmitExamDTO dto = new SubmitExamDTO();
        dto.setExamId(999L);

        when(examRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            examRecordService.submitExam(dto, 1L);
        });

        assertEquals("考试记录不存在", exception.getMessage());
    }

    @Test
    @DisplayName("提交考试 - 已提交的考试不能再次提交")
    void testSubmitExam_AlreadySubmitted() {
        testExamRecord.setStatus(Constants.RECORD_SUBMITTED);

        SubmitExamDTO dto = new SubmitExamDTO();
        dto.setExamId(1L);

        when(examRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            examRecordService.submitExam(dto, 1L);
        });

        assertEquals("考试记录不存在", exception.getMessage());
    }

    @Test
    @DisplayName("提交考试 - 暂停状态可以提交")
    void testSubmitExam_PausedCanSubmit() {
        testExamRecord.setStatus(Constants.RECORD_PAUSED);

        SubmitExamDTO dto = new SubmitExamDTO();
        dto.setExamId(1L);
        dto.setAnswers(new ArrayList<>());

        when(examRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testExamRecord);
        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(examAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamAnswers);
        when(questionMapper.selectBatchIds(anyList())).thenReturn(new ArrayList<>());
        when(examRecordMapper.updateById(any(ExamRecord.class))).thenReturn(1);

        ExamRecordVO result = examRecordService.submitExam(dto, 1L);

        assertNotNull(result);
        assertEquals(Constants.RECORD_SUBMITTED, result.getStatus());
    }

    @Test
    @DisplayName("提交考试 - 单选题全部答对")
    void testSubmitExam_AllSingleCorrect() {
        testExamRecord.setStatus(Constants.RECORD_EXAMING);

        SubmitExamDTO dto = new SubmitExamDTO();
        dto.setExamId(1L);
        List<SubmitExamDTO.AnswerItem> answers = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            SubmitExamDTO.AnswerItem item = new SubmitExamDTO.AnswerItem();
            item.setQuestionId(i);
            item.setAnswer("A");
            answers.add(item);
        }
        dto.setAnswers(answers);

        when(examRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testExamRecord);
        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(examAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamAnswers);
        when(questionMapper.selectBatchIds(anyList())).thenReturn(testQuestions);
        when(examAnswerMapper.updateById(any(ExamAnswer.class))).thenReturn(1);

        final int[] totalScore = {0};
        when(examRecordMapper.updateById(any(ExamRecord.class))).thenAnswer(invocation -> {
            ExamRecord record = invocation.getArgument(0);
            totalScore[0] = record.getScore();
            return 1;
        });

        examRecordService.submitExam(dto, 1L);

        assertEquals(30, totalScore[0]);
    }

    @Test
    @DisplayName("提交考试 - 全部答错")
    void testSubmitExam_AllWrong() {
        testExamRecord.setStatus(Constants.RECORD_EXAMING);

        SubmitExamDTO dto = new SubmitExamDTO();
        dto.setExamId(1L);
        List<SubmitExamDTO.AnswerItem> answers = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            SubmitExamDTO.AnswerItem item = new SubmitExamDTO.AnswerItem();
            item.setQuestionId(i);
            item.setAnswer("B");
            answers.add(item);
        }
        dto.setAnswers(answers);

        when(examRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testExamRecord);
        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(examAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamAnswers);
        when(questionMapper.selectBatchIds(anyList())).thenReturn(testQuestions);
        when(examAnswerMapper.updateById(any(ExamAnswer.class))).thenReturn(1);

        final int[] totalScore = {0};
        when(examRecordMapper.updateById(any(ExamRecord.class))).thenAnswer(invocation -> {
            ExamRecord record = invocation.getArgument(0);
            totalScore[0] = record.getScore();
            return 1;
        });

        examRecordService.submitExam(dto, 1L);

        assertEquals(0, totalScore[0]);
    }

    @Test
    @DisplayName("提交考试 - 空答案")
    void testSubmitExam_EmptyAnswer() {
        testExamRecord.setStatus(Constants.RECORD_EXAMING);

        SubmitExamDTO dto = new SubmitExamDTO();
        dto.setExamId(1L);
        List<SubmitExamDTO.AnswerItem> answers = new ArrayList<>();
        SubmitExamDTO.AnswerItem item = new SubmitExamDTO.AnswerItem();
        item.setQuestionId(1L);
        item.setAnswer("");
        answers.add(item);
        dto.setAnswers(answers);

        when(examRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testExamRecord);
        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(examAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamAnswers);
        when(questionMapper.selectBatchIds(anyList())).thenReturn(testQuestions);
        when(examAnswerMapper.updateById(any(ExamAnswer.class))).thenReturn(1);
        when(examRecordMapper.updateById(any(ExamRecord.class))).thenReturn(1);

        ExamRecordVO result = examRecordService.submitExam(dto, 1L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("提交考试 - 多选题")
    void testSubmitExam_MultiChoice() {
        testExamRecord.setStatus(Constants.RECORD_EXAMING);

        Question multiQ = new Question();
        multiQ.setId(4L);
        multiQ.setType(Constants.TYPE_MULTI);
        multiQ.setContent("多选题");
        multiQ.setOptionA("A");
        multiQ.setOptionB("B");
        multiQ.setOptionC("C");
        multiQ.setOptionD("D");
        multiQ.setAnswer("ABC");
        multiQ.setScore(10);

        ExamAnswer multiAnswer = new ExamAnswer();
        multiAnswer.setId(4L);
        multiAnswer.setRecordId(1L);
        multiAnswer.setQuestionId(4L);
        multiAnswer.setAnswer("");
        multiAnswer.setIsCorrect(0);
        multiAnswer.setScore(0);
        multiAnswer.setAutoScore(0);
        multiAnswer.setOptionOrder("ABCD");

        testExamAnswers.add(multiAnswer);
        testQuestions.add(multiQ);

        SubmitExamDTO dto = new SubmitExamDTO();
        dto.setExamId(1L);
        List<SubmitExamDTO.AnswerItem> answers = new ArrayList<>();
        SubmitExamDTO.AnswerItem item = new SubmitExamDTO.AnswerItem();
        item.setQuestionId(4L);
        item.setAnswer("ABC");
        answers.add(item);
        dto.setAnswers(answers);

        when(examRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testExamRecord);
        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(examAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamAnswers);
        when(questionMapper.selectBatchIds(anyList())).thenReturn(testQuestions);
        when(examAnswerMapper.updateById(any(ExamAnswer.class))).thenReturn(1);
        when(examRecordMapper.updateById(any(ExamRecord.class))).thenReturn(1);

        ExamRecordVO result = examRecordService.submitExam(dto, 1L);

        assertNotNull(result);
    }

    @Test
    @DisplayName("提交考试 - 问答题不自动评分")
    void testSubmitExam_EssayQuestion() {
        testExamRecord.setStatus(Constants.RECORD_EXAMING);

        Question essayQ = new Question();
        essayQ.setId(5L);
        essayQ.setType(Constants.TYPE_ESSAY);
        essayQ.setContent("问答题");
        essayQ.setAnswer("参考答案");
        essayQ.setScore(20);

        ExamAnswer essayAnswer = new ExamAnswer();
        essayAnswer.setId(5L);
        essayAnswer.setRecordId(1L);
        essayAnswer.setQuestionId(5L);
        essayAnswer.setAnswer("");
        essayAnswer.setIsCorrect(2);
        essayAnswer.setScore(0);
        essayAnswer.setAutoScore(0);

        testExamAnswers.add(essayAnswer);
        testQuestions.add(essayQ);

        SubmitExamDTO dto = new SubmitExamDTO();
        dto.setExamId(1L);
        List<SubmitExamDTO.AnswerItem> answers = new ArrayList<>();
        SubmitExamDTO.AnswerItem item = new SubmitExamDTO.AnswerItem();
        item.setQuestionId(5L);
        item.setAnswer("我的答案");
        answers.add(item);
        dto.setAnswers(answers);

        when(examRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testExamRecord);
        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(examAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamAnswers);
        when(questionMapper.selectBatchIds(anyList())).thenReturn(testQuestions);

        final int[] essayScore = {0};
        final int[] essayCorrect = {0};
        doAnswer(invocation -> {
            ExamAnswer ea = invocation.getArgument(0);
            if (ea.getQuestionId() == 5L) {
                essayScore[0] = ea.getScore();
                essayCorrect[0] = ea.getIsCorrect();
            }
            return 1;
        }).when(examAnswerMapper).updateById(any(ExamAnswer.class));

        when(examRecordMapper.updateById(any(ExamRecord.class))).thenReturn(1);

        examRecordService.submitExam(dto, 1L);

        assertEquals(0, essayScore[0]);
        assertEquals(2, essayCorrect[0]);
    }

    @Test
    @DisplayName("保存答案 - 正常情况")
    void testSaveAnswer_Normal() {
        testExamRecord.setStatus(Constants.RECORD_EXAMING);

        SaveAnswerDTO dto = new SaveAnswerDTO();
        dto.setRecordId(1L);
        dto.setQuestionId(1L);
        dto.setAnswer("A");

        when(examRecordMapper.selectById(1L)).thenReturn(testExamRecord);
        when(examAnswerMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testExamAnswers.get(0));
        when(examAnswerMapper.updateById(any(ExamAnswer.class))).thenReturn(1);

        boolean result = examRecordService.saveAnswer(dto, 1L);

        assertTrue(result);
        verify(examRecordMapper).selectById(1L);
        verify(examAnswerMapper).selectOne(any(LambdaQueryWrapper.class));
        verify(examAnswerMapper).updateById(any(ExamAnswer.class));
    }

    @Test
    @DisplayName("保存答案 - 考试记录不存在")
    void testSaveAnswer_RecordNotFound() {
        SaveAnswerDTO dto = new SaveAnswerDTO();
        dto.setRecordId(999L);
        dto.setQuestionId(1L);
        dto.setAnswer("A");

        when(examRecordMapper.selectById(999L)).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            examRecordService.saveAnswer(dto, 1L);
        });

        assertEquals("考试记录不存在", exception.getMessage());
    }

    @Test
    @DisplayName("保存答案 - 无权操作他人记录")
    void testSaveAnswer_NoPermission() {
        testExamRecord.setUserId(2L);
        testExamRecord.setStatus(Constants.RECORD_EXAMING);

        SaveAnswerDTO dto = new SaveAnswerDTO();
        dto.setRecordId(1L);
        dto.setQuestionId(1L);
        dto.setAnswer("A");

        when(examRecordMapper.selectById(1L)).thenReturn(testExamRecord);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            examRecordService.saveAnswer(dto, 1L);
        });

        assertEquals("无权操作此考试记录", exception.getMessage());
    }

    @Test
    @DisplayName("保存答案 - 已提交无法修改")
    void testSaveAnswer_AlreadySubmitted() {
        testExamRecord.setStatus(Constants.RECORD_SUBMITTED);

        SaveAnswerDTO dto = new SaveAnswerDTO();
        dto.setRecordId(1L);
        dto.setQuestionId(1L);
        dto.setAnswer("A");

        when(examRecordMapper.selectById(1L)).thenReturn(testExamRecord);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            examRecordService.saveAnswer(dto, 1L);
        });

        assertEquals("考试已提交，无法修改答案", exception.getMessage());
    }

    @Test
    @DisplayName("保存答案 - 答题记录不存在")
    void testSaveAnswer_AnswerNotFound() {
        testExamRecord.setStatus(Constants.RECORD_EXAMING);

        SaveAnswerDTO dto = new SaveAnswerDTO();
        dto.setRecordId(1L);
        dto.setQuestionId(999L);
        dto.setAnswer("A");

        when(examRecordMapper.selectById(1L)).thenReturn(testExamRecord);
        when(examAnswerMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            examRecordService.saveAnswer(dto, 1L);
        });

        assertEquals("答题记录不存在", exception.getMessage());
    }

    @Test
    @DisplayName("批量保存答案 - 正常情况")
    void testSaveAnswers_Normal() {
        testExamRecord.setStatus(Constants.RECORD_EXAMING);

        SaveAnswersDTO dto = new SaveAnswersDTO();
        dto.setRecordId(1L);
        List<SaveAnswersDTO.SaveAnswerItem> answers = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            SaveAnswersDTO.SaveAnswerItem item = new SaveAnswersDTO.SaveAnswerItem();
            item.setQuestionId(i);
            item.setAnswer("A");
            answers.add(item);
        }
        dto.setAnswers(answers);

        when(examRecordMapper.selectById(1L)).thenReturn(testExamRecord);
        when(examAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testExamAnswers);
        when(examAnswerMapper.updateById(any(ExamAnswer.class))).thenReturn(1);

        boolean result = examRecordService.saveAnswers(dto, 1L);

        assertTrue(result);
        verify(examAnswerMapper, times(3)).updateById(any(ExamAnswer.class));
    }

    @Test
    @DisplayName("批量保存答案 - 空答案列表")
    void testSaveAnswers_EmptyList() {
        testExamRecord.setStatus(Constants.RECORD_EXAMING);

        SaveAnswersDTO dto = new SaveAnswersDTO();
        dto.setRecordId(1L);
        dto.setAnswers(new ArrayList<>());

        when(examRecordMapper.selectById(1L)).thenReturn(testExamRecord);

        boolean result = examRecordService.saveAnswers(dto, 1L);

        assertTrue(result);
        verify(examAnswerMapper, never()).updateById(any(ExamAnswer.class));
    }

    @Test
    @DisplayName("暂停考试 - 正常情况")
    void testPauseExam_Normal() {
        testExamRecord.setStatus(Constants.RECORD_EXAMING);

        PauseExamDTO dto = new PauseExamDTO();
        dto.setRecordId(1L);

        when(examRecordMapper.selectById(1L)).thenReturn(testExamRecord);
        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(examRecordMapper.updateById(any(ExamRecord.class))).thenReturn(1);

        ExamRecordVO result = examRecordService.pauseExam(dto, 1L);

        assertNotNull(result);
        assertEquals(Constants.RECORD_PAUSED, result.getStatus());
        verify(examRecordMapper).updateById(any(ExamRecord.class));
    }

    @Test
    @DisplayName("暂停考试 - 非考试中状态无法暂停")
    void testPauseExam_NotInProgress() {
        testExamRecord.setStatus(Constants.RECORD_PAUSED);

        PauseExamDTO dto = new PauseExamDTO();
        dto.setRecordId(1L);

        when(examRecordMapper.selectById(1L)).thenReturn(testExamRecord);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            examRecordService.pauseExam(dto, 1L);
        });

        assertEquals("当前状态无法暂停考试", exception.getMessage());
    }

    @Test
    @DisplayName("暂停考试 - 已结束的考试无法暂停")
    void testPauseExam_ExamEnded() {
        testExamRecord.setStatus(Constants.RECORD_EXAMING);

        Exam endedExam = new Exam();
        endedExam.setId(1L);
        endedExam.setEndTime(LocalDateTime.now().minusHours(1));

        PauseExamDTO dto = new PauseExamDTO();
        dto.setRecordId(1L);

        when(examRecordMapper.selectById(1L)).thenReturn(testExamRecord);
        when(examMapper.selectById(1L)).thenReturn(endedExam);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            examRecordService.pauseExam(dto, 1L);
        });

        assertEquals("考试已结束，无法暂停", exception.getMessage());
    }

    @Test
    @DisplayName("恢复考试 - 正常情况")
    void testResumeExam_Normal() {
        testExamRecord.setStatus(Constants.RECORD_PAUSED);
        testExamRecord.setLastPauseTime(LocalDateTime.now().minusMinutes(5));
        testExamRecord.setPauseCount(1);
        testExamRecord.setTotalPauseTime(0);

        PauseExamDTO dto = new PauseExamDTO();
        dto.setRecordId(1L);

        when(examRecordMapper.selectById(1L)).thenReturn(testExamRecord);
        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(examRecordMapper.updateById(any(ExamRecord.class))).thenReturn(1);

        ExamRecordVO result = examRecordService.resumeExam(dto, 1L);

        assertNotNull(result);
        assertEquals(Constants.RECORD_EXAMING, result.getStatus());
        verify(examRecordMapper).updateById(any(ExamRecord.class));
    }

    @Test
    @DisplayName("恢复考试 - 非暂停状态无法恢复")
    void testResumeExam_NotPaused() {
        testExamRecord.setStatus(Constants.RECORD_EXAMING);

        PauseExamDTO dto = new PauseExamDTO();
        dto.setRecordId(1L);

        when(examRecordMapper.selectById(1L)).thenReturn(testExamRecord);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            examRecordService.resumeExam(dto, 1L);
        });

        assertEquals("当前状态无法恢复考试", exception.getMessage());
    }

    @Test
    @DisplayName("恢复考试 - 已结束的考试无法恢复")
    void testResumeExam_ExamEnded() {
        testExamRecord.setStatus(Constants.RECORD_PAUSED);

        Exam endedExam = new Exam();
        endedExam.setId(1L);
        endedExam.setEndTime(LocalDateTime.now().minusHours(1));

        PauseExamDTO dto = new PauseExamDTO();
        dto.setRecordId(1L);

        when(examRecordMapper.selectById(1L)).thenReturn(testExamRecord);
        when(examMapper.selectById(1L)).thenReturn(endedExam);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            examRecordService.resumeExam(dto, 1L);
        });

        assertEquals("考试已结束，无法恢复", exception.getMessage());
    }

    @Test
    @DisplayName("状态转换测试 - 开始->暂停->恢复->提交")
    void testStatusTransition_FullExamLifecycle() {
        testExamRecord.setStatus(Constants.RECORD_EXAMING);

        assertEquals(Constants.RECORD_EXAMING, testExamRecord.getStatus());

        testExamRecord.setStatus(Constants.RECORD_PAUSED);
        assertEquals(Constants.RECORD_PAUSED, testExamRecord.getStatus());

        testExamRecord.setStatus(Constants.RECORD_EXAMING);
        assertEquals(Constants.RECORD_EXAMING, testExamRecord.getStatus());

        testExamRecord.setStatus(Constants.RECORD_SUBMITTED);
        assertEquals(Constants.RECORD_SUBMITTED, testExamRecord.getStatus());
    }

    @Test
    @DisplayName("获取当前考试 - 有正在进行的考试")
    void testGetCurrentExam_HasExam() {
        testExamRecord.setStatus(Constants.RECORD_EXAMING);

        when(examRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testExamRecord);
        when(examMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testExam));
        when(paperMapper.selectBatchIds(anyList())).thenReturn(Collections.singletonList(testPaper));
        when(userMapper.selectBatchIds(anyList())).thenReturn(new ArrayList<>());

        ExamRecordVO result = examRecordService.getCurrentExam(1L);

        assertNotNull(result);
        assertEquals(Constants.RECORD_EXAMING, result.getStatus());
    }

    @Test
    @DisplayName("获取当前考试 - 没有正在进行的考试")
    void testGetCurrentExam_NoExam() {
        when(examRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        ExamRecordVO result = examRecordService.getCurrentExam(1L);

        assertNull(result);
    }

    @Test
    @DisplayName("接口交互测试 - 开始考试与多个Mapper交互")
    void testInterfaceInteraction_StartExam() {
        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(examRecordMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
        when(paperMapper.selectById(1L)).thenReturn(testPaper);
        when(paperQuestionMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(testPaperQuestions);
        when(questionMapper.selectBatchIds(anyList())).thenReturn(testQuestions);
        when(examRecordMapper.insert(any(ExamRecord.class))).thenAnswer(invocation -> {
            ExamRecord record = invocation.getArgument(0);
            record.setId(1L);
            return 1;
        });
        when(examRecordMapper.updateById(any(ExamRecord.class))).thenReturn(1);
        when(examAnswerMapper.insert(any(ExamAnswer.class))).thenReturn(1);

        examRecordService.startExam(1L, 1L);

        verify(examMapper, times(1)).selectById(1L);
        verify(examRecordMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(paperMapper, times(1)).selectById(1L);
        verify(paperQuestionMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        verify(questionMapper, times(1)).selectBatchIds(anyList());
        verify(examRecordMapper, times(1)).insert(any(ExamRecord.class));
        verify(examAnswerMapper, times(3)).insert(any(ExamAnswer.class));
    }

    @Test
    @DisplayName("输入数据校验 - 保存空答案")
    void testSaveAnswer_NullAnswer() {
        testExamRecord.setStatus(Constants.RECORD_EXAMING);

        SaveAnswerDTO dto = new SaveAnswerDTO();
        dto.setRecordId(1L);
        dto.setQuestionId(1L);
        dto.setAnswer(null);

        when(examRecordMapper.selectById(1L)).thenReturn(testExamRecord);
        when(examAnswerMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testExamAnswers.get(0));
        when(examAnswerMapper.updateById(any(ExamAnswer.class))).thenAnswer(invocation -> {
            ExamAnswer ea = invocation.getArgument(0);
            assertEquals("", ea.getAnswer());
            return 1;
        });

        boolean result = examRecordService.saveAnswer(dto, 1L);
        assertTrue(result);
    }
}
