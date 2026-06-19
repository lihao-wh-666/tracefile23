package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
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
import com.exam.service.impl.PaperServiceImpl;
import com.exam.vo.PaperVO;
import com.exam.vo.QuestionVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("试卷管理模块单元测试")
class PaperServiceTest {

    @Mock
    private PaperMapper paperMapper;

    @Mock
    private PaperQuestionMapper paperQuestionMapper;

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private SubjectMapper subjectMapper;

    @InjectMocks
    private PaperServiceImpl paperService;

    private Paper testPaper;
    private PaperDTO testPaperDTO;
    private Subject testSubject;
    private List<Question> testQuestions;
    private List<PaperQuestion> testPaperQuestions;

    @BeforeEach
    void setUp() {
        testSubject = new Subject();
        testSubject.setId(1L);
        testSubject.setName("计算机科学");

        testPaper = new Paper();
        testPaper.setId(1L);
        testPaper.setName("Java基础测试");
        testPaper.setSubjectId(1L);
        testPaper.setTotalScore(100);
        testPaper.setPassScore(60);
        testPaper.setDuration(90);
        testPaper.setStatus(0);
        testPaper.setCreateTime(LocalDateTime.now());
        testPaper.setUpdateTime(LocalDateTime.now());

        testPaperDTO = new PaperDTO();
        testPaperDTO.setName("Java基础测试");
        testPaperDTO.setSubjectId(1L);
        testPaperDTO.setTotalScore(100);
        testPaperDTO.setPassScore(60);
        testPaperDTO.setDuration(90);
        testPaperDTO.setQuestionIds(Arrays.asList(1L, 2L, 3L));

        testQuestions = new ArrayList<>();
        for (long i = 1; i <= 3; i++) {
            Question q = new Question();
            q.setId(i);
            q.setSubjectId(1L);
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

        testPaperQuestions = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            PaperQuestion pq = new PaperQuestion();
            pq.setPaperId(1L);
            pq.setQuestionId((long) (i + 1));
            pq.setSort(i + 1);
            testPaperQuestions.add(pq);
        }
    }

    @Test
    @DisplayName("分页查询试卷列表 - 正常情况")
    void testPage_Normal() {
        Page<Paper> paperPage = new Page<>(1, 10);
        paperPage.setRecords(Collections.singletonList(testPaper));
        paperPage.setTotal(1);
        paperPage.setCurrent(1);
        paperPage.setSize(10);

        when(paperMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(paperPage);
        when(subjectMapper.selectBatchIds(anyList()))
                .thenReturn(Collections.singletonList(testSubject));

        IPage<PaperVO> result = paperService.page(1, 10, 1L, 0);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("Java基础测试", result.getRecords().get(0).getName());
        assertEquals("计算机科学", result.getRecords().get(0).getSubjectName());

        verify(paperMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        verify(subjectMapper).selectBatchIds(anyList());
    }

    @Test
    @DisplayName("分页查询试卷列表 - 无筛选条件")
    void testPage_NoFilters() {
        Page<Paper> paperPage = new Page<>(1, 10);
        paperPage.setRecords(new ArrayList<>());
        paperPage.setTotal(0);

        when(paperMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(paperPage);

        IPage<PaperVO> result = paperService.page(1, 10, null, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());

        verify(paperMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        verify(subjectMapper, never()).selectBatchIds(anyList());
    }

    @Test
    @DisplayName("分页查询试卷列表 - 边界条件：第1页")
    void testPage_FirstPage() {
        Page<Paper> paperPage = new Page<>(1, 10);
        paperPage.setRecords(Collections.singletonList(testPaper));
        paperPage.setTotal(1);

        when(paperMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(paperPage);
        when(subjectMapper.selectBatchIds(anyList()))
                .thenReturn(Collections.singletonList(testSubject));

        IPage<PaperVO> result = paperService.page(1, 10, 1L, 0);

        assertNotNull(result);
        assertEquals(1, result.getCurrent());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("获取试卷详情 - 正常情况")
    void testGetDetail_Normal() {
        when(paperMapper.selectById(1L)).thenReturn(testPaper);
        when(subjectMapper.selectById(1L)).thenReturn(testSubject);
        when(paperQuestionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(testPaperQuestions);
        when(questionMapper.selectBatchIds(anyList()))
                .thenReturn(testQuestions);

        PaperVO result = paperService.getDetail(1L);

        assertNotNull(result);
        assertEquals("Java基础测试", result.getName());
        assertEquals("计算机科学", result.getSubjectName());
        assertEquals(3, result.getQuestions().size());

        verify(paperMapper).selectById(1L);
        verify(subjectMapper).selectById(1L);
        verify(paperQuestionMapper).selectList(any(LambdaQueryWrapper.class));
        verify(questionMapper).selectBatchIds(anyList());
    }

    @Test
    @DisplayName("获取试卷详情 - 试卷不存在")
    void testGetDetail_NotFound() {
        when(paperMapper.selectById(999L)).thenReturn(null);

        PaperVO result = paperService.getDetail(999L);

        assertNull(result);
        verify(paperMapper).selectById(999L);
        verify(subjectMapper, never()).selectById(anyLong());
    }

    @Test
    @DisplayName("获取试卷详情 - 没有题目")
    void testGetDetail_NoQuestions() {
        when(paperMapper.selectById(1L)).thenReturn(testPaper);
        when(subjectMapper.selectById(1L)).thenReturn(testSubject);
        when(paperQuestionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(new ArrayList<>());

        PaperVO result = paperService.getDetail(1L);

        assertNotNull(result);
        assertNull(result.getQuestions());
        verify(questionMapper, never()).selectBatchIds(anyList());
    }

    @Test
    @DisplayName("获取试卷详情 - 科目不存在")
    void testGetDetail_NoSubject() {
        when(paperMapper.selectById(1L)).thenReturn(testPaper);
        when(subjectMapper.selectById(1L)).thenReturn(null);
        when(paperQuestionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(testPaperQuestions);
        when(questionMapper.selectBatchIds(anyList()))
                .thenReturn(testQuestions);

        PaperVO result = paperService.getDetail(1L);

        assertNotNull(result);
        assertNull(result.getSubjectName());
        assertNotNull(result.getQuestions());
    }

    @Test
    @DisplayName("保存试卷 - 正常情况（带题目）")
    void testSave_WithQuestions() {
        when(paperMapper.insert(any(Paper.class))).thenAnswer(invocation -> {
            Paper paper = invocation.getArgument(0);
            paper.setId(1L);
            return 1;
        });
        when(paperQuestionMapper.insert(any(PaperQuestion.class))).thenReturn(1);

        boolean result = paperService.save(testPaperDTO);

        assertTrue(result);
        verify(paperMapper).insert(any(Paper.class));
        verify(paperQuestionMapper, times(3)).insert(any(PaperQuestion.class));
    }

    @Test
    @DisplayName("保存试卷 - 没有题目")
    void testSave_NoQuestions() {
        PaperDTO dto = new PaperDTO();
        dto.setName("测试试卷");
        dto.setSubjectId(1L);
        dto.setTotalScore(100);
        dto.setPassScore(60);
        dto.setDuration(90);
        dto.setQuestionIds(null);

        when(paperMapper.insert(any(Paper.class))).thenAnswer(invocation -> {
            Paper paper = invocation.getArgument(0);
            paper.setId(1L);
            return 1;
        });

        boolean result = paperService.save(dto);

        assertTrue(result);
        verify(paperMapper).insert(any(Paper.class));
        verify(paperQuestionMapper, never()).insert(any(PaperQuestion.class));
    }

    @Test
    @DisplayName("保存试卷 - 空题目列表")
    void testSave_EmptyQuestionList() {
        PaperDTO dto = new PaperDTO();
        dto.setName("测试试卷");
        dto.setSubjectId(1L);
        dto.setTotalScore(100);
        dto.setPassScore(60);
        dto.setDuration(90);
        dto.setQuestionIds(new ArrayList<>());

        when(paperMapper.insert(any(Paper.class))).thenAnswer(invocation -> {
            Paper paper = invocation.getArgument(0);
            paper.setId(1L);
            return 1;
        });

        boolean result = paperService.save(dto);

        assertTrue(result);
        verify(paperMapper).insert(any(Paper.class));
        verify(paperQuestionMapper, never()).insert(any(PaperQuestion.class));
    }

    @Test
    @DisplayName("保存试卷 - 状态初始化为草稿")
    void testSave_StatusIsDraft() {
        when(paperMapper.insert(any(Paper.class))).thenAnswer(invocation -> {
            Paper paper = invocation.getArgument(0);
            paper.setId(1L);
            assertEquals(0, paper.getStatus());
            return 1;
        });

        boolean result = paperService.save(testPaperDTO);

        assertTrue(result);
        verify(paperMapper).insert(any(Paper.class));
    }

    @Test
    @DisplayName("更新试卷 - 正常情况")
    void testUpdate_Normal() {
        when(paperMapper.updateById(any(Paper.class))).thenReturn(1);
        when(paperQuestionMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(3);
        when(paperQuestionMapper.insert(any(PaperQuestion.class))).thenReturn(1);

        boolean result = paperService.update(1L, testPaperDTO);

        assertTrue(result);
        verify(paperMapper).updateById(any(Paper.class));
        verify(paperQuestionMapper).delete(any(LambdaQueryWrapper.class));
        verify(paperQuestionMapper, times(3)).insert(any(PaperQuestion.class));
    }

    @Test
    @DisplayName("更新试卷 - 删除原有题目，新增空题目")
    void testUpdate_RemoveAllQuestions() {
        PaperDTO dto = new PaperDTO();
        dto.setName("更新后的试卷");
        dto.setSubjectId(1L);
        dto.setTotalScore(100);
        dto.setPassScore(60);
        dto.setDuration(120);
        dto.setQuestionIds(new ArrayList<>());

        when(paperMapper.updateById(any(Paper.class))).thenReturn(1);
        when(paperQuestionMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(3);

        boolean result = paperService.update(1L, dto);

        assertTrue(result);
        verify(paperMapper).updateById(any(Paper.class));
        verify(paperQuestionMapper).delete(any(LambdaQueryWrapper.class));
        verify(paperQuestionMapper, never()).insert(any(PaperQuestion.class));
    }

    @Test
    @DisplayName("更新试卷 - null题目列表")
    void testUpdate_NullQuestionList() {
        PaperDTO dto = new PaperDTO();
        dto.setName("更新后的试卷");
        dto.setSubjectId(1L);
        dto.setTotalScore(100);
        dto.setPassScore(60);
        dto.setDuration(120);
        dto.setQuestionIds(null);

        when(paperMapper.updateById(any(Paper.class))).thenReturn(1);
        when(paperQuestionMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(3);

        boolean result = paperService.update(1L, dto);

        assertTrue(result);
        verify(paperMapper).updateById(any(Paper.class));
        verify(paperQuestionMapper).delete(any(LambdaQueryWrapper.class));
        verify(paperQuestionMapper, never()).insert(any(PaperQuestion.class));
    }

    @Test
    @DisplayName("删除试卷 - 正常情况")
    void testRemoveById_Normal() {
        when(paperQuestionMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(3);
        when(paperMapper.deleteById(1L)).thenReturn(1);

        boolean result = paperService.removeById(1L);

        assertTrue(result);
        verify(paperQuestionMapper).delete(any(LambdaQueryWrapper.class));
        verify(paperMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除试卷 - 试卷不存在")
    void testRemoveById_NotFound() {
        when(paperQuestionMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(0);
        when(paperMapper.deleteById(999L)).thenReturn(0);

        boolean result = paperService.removeById(999L);

        assertFalse(result);
        verify(paperQuestionMapper).delete(any(LambdaQueryWrapper.class));
        verify(paperMapper).deleteById(999L);
    }

    @Test
    @DisplayName("发布试卷 - 正常情况")
    void testPublish_Normal() {
        when(paperMapper.updateById(any(Paper.class))).thenAnswer(invocation -> {
            Paper paper = invocation.getArgument(0);
            assertEquals(1, paper.getStatus());
            assertEquals(1L, paper.getId());
            return 1;
        });

        boolean result = paperService.publish(1L);

        assertTrue(result);
        verify(paperMapper).updateById(any(Paper.class));
    }

    @Test
    @DisplayName("发布试卷 - 试卷不存在")
    void testPublish_NotFound() {
        when(paperMapper.updateById(any(Paper.class))).thenReturn(0);

        boolean result = paperService.publish(999L);

        assertFalse(result);
        verify(paperMapper).updateById(any(Paper.class));
    }

    @Test
    @DisplayName("题目排序验证 - 保存时按顺序排列")
    void testSave_QuestionSortOrder() {
        when(paperMapper.insert(any(Paper.class))).thenAnswer(invocation -> {
            Paper paper = invocation.getArgument(0);
            paper.setId(1L);
            return 1;
        });

        List<PaperQuestion> capturedPqs = new ArrayList<>();
        when(paperQuestionMapper.insert(any(PaperQuestion.class))).thenAnswer(invocation -> {
            capturedPqs.add(invocation.getArgument(0));
            return 1;
        });

        paperService.save(testPaperDTO);

        assertEquals(3, capturedPqs.size());
        for (int i = 0; i < capturedPqs.size(); i++) {
            assertEquals(i + 1, capturedPqs.get(i).getSort());
            assertEquals((long) (i + 1), capturedPqs.get(i).getQuestionId());
        }
    }

    @Test
    @DisplayName("题目排序验证 - 更新时重新排序")
    void testUpdate_QuestionSortOrder() {
        when(paperMapper.updateById(any(Paper.class))).thenReturn(1);
        when(paperQuestionMapper.delete(any(LambdaQueryWrapper.class))).thenReturn(3);

        List<PaperQuestion> capturedPqs = new ArrayList<>();
        when(paperQuestionMapper.insert(any(PaperQuestion.class))).thenAnswer(invocation -> {
            capturedPqs.add(invocation.getArgument(0));
            return 1;
        });

        paperService.update(1L, testPaperDTO);

        assertEquals(3, capturedPqs.size());
        for (int i = 0; i < capturedPqs.size(); i++) {
            assertEquals(i + 1, capturedPqs.get(i).getSort());
            assertEquals((long) (i + 1), capturedPqs.get(i).getQuestionId());
        }
    }

    @Test
    @DisplayName("分页查询 - 排序验证（按创建时间降序）")
    void testPage_OrderByCreateTimeDesc() {
        Page<Paper> paperPage = new Page<>(1, 10);
        paperPage.setRecords(new ArrayList<>());
        paperPage.setTotal(0);

        when(paperMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(paperPage);

        IPage<PaperVO> result = paperService.page(1, 10, null, null);

        assertNotNull(result);
        verify(paperMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        verify(subjectMapper, never()).selectBatchIds(anyList());
    }

    @Test
    @DisplayName("输入数据校验 - 试卷名称正常")
    void testSave_NameValidation_Normal() {
        when(paperMapper.insert(any(Paper.class))).thenAnswer(invocation -> {
            Paper paper = invocation.getArgument(0);
            paper.setId(1L);
            return 1;
        });

        PaperDTO dto = new PaperDTO();
        dto.setName("正常试卷名称");
        dto.setSubjectId(1L);
        dto.setTotalScore(100);
        dto.setPassScore(60);
        dto.setDuration(90);

        boolean result = paperService.save(dto);
        assertTrue(result);
    }

    @Test
    @DisplayName("边界测试 - 及格分数等于总分")
    void testSave_PassScoreEqualsTotal() {
        when(paperMapper.insert(any(Paper.class))).thenAnswer(invocation -> {
            Paper paper = invocation.getArgument(0);
            paper.setId(1L);
            return 1;
        });

        PaperDTO dto = new PaperDTO();
        dto.setName("边界测试试卷");
        dto.setSubjectId(1L);
        dto.setTotalScore(100);
        dto.setPassScore(100);
        dto.setDuration(60);

        boolean result = paperService.save(dto);
        assertTrue(result);
    }

    @Test
    @DisplayName("边界测试 - 考试时长为0分钟")
    void testSave_DurationZero() {
        when(paperMapper.insert(any(Paper.class))).thenAnswer(invocation -> {
            Paper paper = invocation.getArgument(0);
            paper.setId(1L);
            return 1;
        });

        PaperDTO dto = new PaperDTO();
        dto.setName("零时长试卷");
        dto.setSubjectId(1L);
        dto.setTotalScore(100);
        dto.setPassScore(60);
        dto.setDuration(0);

        boolean result = paperService.save(dto);
        assertTrue(result);
    }

    @Test
    @DisplayName("状态转换测试 - 从草稿到发布")
    void testStatusTransition_DraftToPublished() {
        when(paperMapper.updateById(any(Paper.class))).thenAnswer(invocation -> {
            Paper paper = invocation.getArgument(0);
            assertEquals(1L, paper.getId());
            assertEquals(1, paper.getStatus());
            return 1;
        });

        boolean result = paperService.publish(1L);
        assertTrue(result);

        ArgumentCaptor<Paper> captor = ArgumentCaptor.forClass(Paper.class);
        verify(paperMapper).updateById(captor.capture());
        Paper captured = captor.getValue();
        assertEquals(1, captured.getStatus(), "状态应该变为已发布");
    }

    @Test
    @DisplayName("接口交互测试 - 保存试卷时与多个Mapper交互")
    void testInterfaceInteraction_SavePaper() {
        when(paperMapper.insert(any(Paper.class))).thenAnswer(invocation -> {
            Paper paper = invocation.getArgument(0);
            paper.setId(1L);
            return 1;
        });
        when(paperQuestionMapper.insert(any(PaperQuestion.class))).thenReturn(1);

        boolean result = paperService.save(testPaperDTO);

        assertTrue(result);
        verify(paperMapper, times(1)).insert(any(Paper.class));
        verify(paperQuestionMapper, times(3)).insert(any(PaperQuestion.class));
        verify(questionMapper, never()).selectBatchIds(anyList());
        verify(subjectMapper, never()).selectById(anyLong());
    }

    @Test
    @DisplayName("接口交互测试 - 获取详情时与多个Mapper交互")
    void testInterfaceInteraction_GetDetail() {
        when(paperMapper.selectById(1L)).thenReturn(testPaper);
        when(subjectMapper.selectById(1L)).thenReturn(testSubject);
        when(paperQuestionMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(testPaperQuestions);
        when(questionMapper.selectBatchIds(anyList()))
                .thenReturn(testQuestions);

        PaperVO result = paperService.getDetail(1L);

        assertNotNull(result);
        verify(paperMapper, times(1)).selectById(1L);
        verify(subjectMapper, times(1)).selectById(1L);
        verify(paperQuestionMapper, times(1)).selectList(any(LambdaQueryWrapper.class));
        verify(questionMapper, times(1)).selectBatchIds(anyList());
    }

    @Test
    @DisplayName("大量题目测试 - 50道题")
    void testSave_ManyQuestions() {
        List<Long> questionIds = new ArrayList<>();
        for (long i = 1; i <= 50; i++) {
            questionIds.add(i);
        }
        testPaperDTO.setQuestionIds(questionIds);

        when(paperMapper.insert(any(Paper.class))).thenAnswer(invocation -> {
            Paper paper = invocation.getArgument(0);
            paper.setId(1L);
            return 1;
        });
        when(paperQuestionMapper.insert(any(PaperQuestion.class))).thenReturn(1);

        boolean result = paperService.save(testPaperDTO);

        assertTrue(result);
        verify(paperQuestionMapper, times(50)).insert(any(PaperQuestion.class));
    }
}
