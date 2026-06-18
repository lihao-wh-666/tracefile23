package com.exam.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Constants;
import com.exam.dto.ExamDTO;
import com.exam.entity.Exam;
import com.exam.entity.Paper;
import com.exam.mapper.ExamMapper;
import com.exam.mapper.PaperMapper;
import com.exam.service.impl.ExamServiceImpl;
import com.exam.vo.ExamVO;
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
@DisplayName("考试管理模块单元测试")
class ExamServiceTest {

    @Mock
    private ExamMapper examMapper;

    @Mock
    private PaperMapper paperMapper;

    @InjectMocks
    private ExamServiceImpl examService;

    private Exam testExam;
    private ExamDTO testExamDTO;
    private Paper testPaper;

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
        testExam.setStartTime(LocalDateTime.now().plusDays(1));
        testExam.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        testExam.setStatus(Constants.EXAM_NOT_START);
        testExam.setCreateTime(LocalDateTime.now());
        testExam.setUpdateTime(LocalDateTime.now());

        testExamDTO = new ExamDTO();
        testExamDTO.setPaperId(1L);
        testExamDTO.setName("Java期中考试");
        testExamDTO.setStartTime(LocalDateTime.now().plusDays(1));
        testExamDTO.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
    }

    @Test
    @DisplayName("分页查询考试列表 - 正常情况")
    void testPage_Normal() {
        Page<Exam> examPage = new Page<>(1, 10);
        examPage.setRecords(Collections.singletonList(testExam));
        examPage.setTotal(1);

        when(examMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(examPage);
        when(paperMapper.selectBatchIds(anyList()))
                .thenReturn(Collections.singletonList(testPaper));

        IPage<ExamVO> result = examService.page(1, 10, null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals("Java期中考试", result.getRecords().get(0).getName());
        assertEquals("Java基础试卷", result.getRecords().get(0).getPaperName());

        verify(examMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        verify(paperMapper).selectBatchIds(anyList());
    }

    @Test
    @DisplayName("分页查询考试列表 - 按状态筛选")
    void testPage_FilterByStatus() {
        Exam exam1 = new Exam();
        exam1.setId(1L);
        exam1.setPaperId(1L);
        exam1.setName("未开始考试");
        exam1.setStartTime(LocalDateTime.now().plusDays(1));
        exam1.setEndTime(LocalDateTime.now().plusDays(2));
        exam1.setStatus(Constants.EXAM_NOT_START);

        Exam exam2 = new Exam();
        exam2.setId(2L);
        exam2.setPaperId(1L);
        exam2.setName("已结束考试");
        exam2.setStartTime(LocalDateTime.now().minusDays(2));
        exam2.setEndTime(LocalDateTime.now().minusDays(1));
        exam2.setStatus(Constants.EXAM_END);

        List<Exam> exams = new ArrayList<>();
        exams.add(exam1);
        exams.add(exam2);

        Page<Exam> examPage = new Page<>(1, 10);
        examPage.setRecords(exams);
        examPage.setTotal(2);

        when(examMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(examPage);
        when(paperMapper.selectBatchIds(anyList()))
                .thenReturn(Collections.singletonList(testPaper));

        IPage<ExamVO> result = examService.page(1, 10, Constants.EXAM_NOT_START);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals("未开始考试", result.getRecords().get(0).getName());
    }

    @Test
    @DisplayName("分页查询考试列表 - 无数据")
    void testPage_Empty() {
        Page<Exam> examPage = new Page<>(1, 10);
        examPage.setRecords(new ArrayList<>());
        examPage.setTotal(0);

        when(examMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(examPage);

        IPage<ExamVO> result = examService.page(1, 10, null);

        assertNotNull(result);
        assertEquals(0, result.getTotal());
        assertTrue(result.getRecords().isEmpty());
        verify(paperMapper, never()).selectBatchIds(anyList());
    }

    @Test
    @DisplayName("分页查询考试列表 - 排序验证（按创建时间降序）")
    void testPage_OrderByCreateTimeDesc() {
        Page<Exam> examPage = new Page<>(1, 10);
        examPage.setRecords(new ArrayList<>());
        examPage.setTotal(0);

        when(examMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenAnswer(invocation -> {
                    Page<Exam> page = invocation.getArgument(0);
                    List<OrderItem> orders = page.orders();
                    boolean hasCreateTimeDesc = orders.stream()
                            .anyMatch(o -> "create_time".equals(o.getColumn()) && !o.isAsc());
                    assertTrue(hasCreateTimeDesc, "应该按创建时间降序排列");
                    return examPage;
                });

        examService.page(1, 10, null);
    }

    @Test
    @DisplayName("获取考试详情 - 正常情况")
    void testGetDetail_Normal() {
        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(paperMapper.selectById(1L)).thenReturn(testPaper);

        ExamVO result = examService.getDetail(1L);

        assertNotNull(result);
        assertEquals("Java期中考试", result.getName());
        assertEquals("Java基础试卷", result.getPaperName());
        assertNotNull(result.getStatus());

        verify(examMapper).selectById(1L);
        verify(paperMapper).selectById(1L);
    }

    @Test
    @DisplayName("获取考试详情 - 考试不存在")
    void testGetDetail_NotFound() {
        when(examMapper.selectById(999L)).thenReturn(null);

        ExamVO result = examService.getDetail(999L);

        assertNull(result);
        verify(examMapper).selectById(999L);
        verify(paperMapper, never()).selectById(anyLong());
    }

    @Test
    @DisplayName("获取考试详情 - 试卷不存在")
    void testGetDetail_NoPaper() {
        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(paperMapper.selectById(1L)).thenReturn(null);

        ExamVO result = examService.getDetail(1L);

        assertNotNull(result);
        assertNull(result.getPaperName());
        verify(paperMapper).selectById(1L);
    }

    @Test
    @DisplayName("保存考试 - 正常情况")
    void testSave_Normal() {
        when(examMapper.insert(any(Exam.class))).thenAnswer(invocation -> {
            Exam exam = invocation.getArgument(0);
            exam.setId(1L);
            return 1;
        });

        boolean result = examService.save(testExamDTO);

        assertTrue(result);
        verify(examMapper).insert(any(Exam.class));
    }

    @Test
    @DisplayName("保存考试 - 状态为未开始（未来时间）")
    void testSave_StatusNotStarted() {
        testExamDTO.setStartTime(LocalDateTime.now().plusDays(1));
        testExamDTO.setEndTime(LocalDateTime.now().plusDays(2));

        when(examMapper.insert(any(Exam.class))).thenAnswer(invocation -> {
            Exam exam = invocation.getArgument(0);
            exam.setId(1L);
            assertEquals(Constants.EXAM_NOT_START, exam.getStatus());
            return 1;
        });

        boolean result = examService.save(testExamDTO);
        assertTrue(result);
    }

    @Test
    @DisplayName("保存考试 - 状态为进行中")
    void testSave_StatusInProgress() {
        ExamDTO dto = new ExamDTO();
        dto.setPaperId(1L);
        dto.setName("进行中的考试");
        dto.setStartTime(LocalDateTime.now().minusHours(1));
        dto.setEndTime(LocalDateTime.now().plusHours(1));

        when(examMapper.insert(any(Exam.class))).thenAnswer(invocation -> {
            Exam exam = invocation.getArgument(0);
            exam.setId(1L);
            assertEquals(Constants.EXAM_ING, exam.getStatus());
            return 1;
        });

        boolean result = examService.save(dto);
        assertTrue(result);
    }

    @Test
    @DisplayName("保存考试 - 状态为已结束")
    void testSave_StatusEnded() {
        ExamDTO dto = new ExamDTO();
        dto.setPaperId(1L);
        dto.setName("已结束的考试");
        dto.setStartTime(LocalDateTime.now().minusDays(2));
        dto.setEndTime(LocalDateTime.now().minusDays(1));

        when(examMapper.insert(any(Exam.class))).thenAnswer(invocation -> {
            Exam exam = invocation.getArgument(0);
            exam.setId(1L);
            assertEquals(Constants.EXAM_END, exam.getStatus());
            return 1;
        });

        boolean result = examService.save(dto);
        assertTrue(result);
    }

    @Test
    @DisplayName("保存考试 - 开始时间等于结束时间（边界）")
    void testSave_StartTimeEqualsEndTime() {
        ExamDTO dto = new ExamDTO();
        dto.setPaperId(1L);
        dto.setName("边界考试");
        LocalDateTime time = LocalDateTime.now().plusDays(1);
        dto.setStartTime(time);
        dto.setEndTime(time);

        when(examMapper.insert(any(Exam.class))).thenAnswer(invocation -> {
            Exam exam = invocation.getArgument(0);
            exam.setId(1L);
            return 1;
        });

        boolean result = examService.save(dto);
        assertTrue(result);
    }

    @Test
    @DisplayName("保存考试 - 插入失败")
    void testSave_InsertFailed() {
        when(examMapper.insert(any(Exam.class))).thenReturn(0);

        boolean result = examService.save(testExamDTO);

        assertFalse(result);
        verify(examMapper).insert(any(Exam.class));
    }

    @Test
    @DisplayName("更新考试 - 正常情况")
    void testUpdate_Normal() {
        when(examMapper.updateById(any(Exam.class))).thenReturn(1);

        boolean result = examService.update(1L, testExamDTO);

        assertTrue(result);
        verify(examMapper).updateById(any(Exam.class));
    }

    @Test
    @DisplayName("更新考试 - 更新状态")
    void testUpdate_UpdateStatus() {
        ExamDTO dto = new ExamDTO();
        dto.setPaperId(1L);
        dto.setName("更新后的考试");
        dto.setStartTime(LocalDateTime.now().minusHours(1));
        dto.setEndTime(LocalDateTime.now().plusHours(1));

        when(examMapper.updateById(any(Exam.class))).thenAnswer(invocation -> {
            Exam exam = invocation.getArgument(0);
            assertEquals(1L, exam.getId());
            assertEquals(Constants.EXAM_ING, exam.getStatus());
            return 1;
        });

        boolean result = examService.update(1L, dto);
        assertTrue(result);
    }

    @Test
    @DisplayName("更新考试 - 考试不存在")
    void testUpdate_NotFound() {
        when(examMapper.updateById(any(Exam.class))).thenReturn(0);

        boolean result = examService.update(999L, testExamDTO);

        assertFalse(result);
        verify(examMapper).updateById(any(Exam.class));
    }

    @Test
    @DisplayName("删除考试 - 正常情况")
    void testRemoveById_Normal() {
        when(examMapper.deleteById(1L)).thenReturn(1);

        boolean result = examService.removeById(1L);

        assertTrue(result);
        verify(examMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除考试 - 考试不存在")
    void testRemoveById_NotFound() {
        when(examMapper.deleteById(999L)).thenReturn(0);

        boolean result = examService.removeById(999L);

        assertFalse(result);
        verify(examMapper).deleteById(999L);
    }

    @Test
    @DisplayName("状态转换测试 - 未开始 -> 进行中 -> 已结束")
    void testStatusTransition_FullLifecycle() {
        ExamDTO notStartDto = new ExamDTO();
        notStartDto.setPaperId(1L);
        notStartDto.setName("生命周期测试");
        notStartDto.setStartTime(LocalDateTime.now().plusDays(1));
        notStartDto.setEndTime(LocalDateTime.now().plusDays(2));

        List<Integer> capturedStatuses = new ArrayList<>();
        when(examMapper.insert(any(Exam.class))).thenAnswer(invocation -> {
            Exam exam = invocation.getArgument(0);
            exam.setId(1L);
            capturedStatuses.add(exam.getStatus());
            return 1;
        });

        examService.save(notStartDto);
        assertEquals(Constants.EXAM_NOT_START, capturedStatuses.get(0));

        ExamDTO inProgressDto = new ExamDTO();
        inProgressDto.setPaperId(1L);
        inProgressDto.setName("进行中考试");
        inProgressDto.setStartTime(LocalDateTime.now().minusHours(1));
        inProgressDto.setEndTime(LocalDateTime.now().plusHours(1));

        when(examMapper.updateById(any(Exam.class))).thenAnswer(invocation -> {
            Exam exam = invocation.getArgument(0);
            capturedStatuses.add(exam.getStatus());
            return 1;
        });

        examService.update(1L, inProgressDto);
        assertEquals(Constants.EXAM_ING, capturedStatuses.get(1));

        ExamDTO endedDto = new ExamDTO();
        endedDto.setPaperId(1L);
        endedDto.setName("已结束考试");
        endedDto.setStartTime(LocalDateTime.now().minusDays(2));
        endedDto.setEndTime(LocalDateTime.now().minusDays(1));

        examService.update(1L, endedDto);
        assertEquals(Constants.EXAM_END, capturedStatuses.get(2));

        assertEquals(3, capturedStatuses.size());
    }

    @Test
    @DisplayName("输入数据校验 - 正常考试时间")
    void testSave_ValidTimeRange() {
        when(examMapper.insert(any(Exam.class))).thenAnswer(invocation -> {
            Exam exam = invocation.getArgument(0);
            exam.setId(1L);
            return 1;
        });

        boolean result = examService.save(testExamDTO);
        assertTrue(result);
    }

    @Test
    @DisplayName("边界测试 - 极短考试（1分钟）")
    void testSave_VeryShortExam() {
        ExamDTO dto = new ExamDTO();
        dto.setPaperId(1L);
        dto.setName("极短考试");
        dto.setStartTime(LocalDateTime.now().plusHours(1));
        dto.setEndTime(LocalDateTime.now().plusHours(1).plusMinutes(1));

        when(examMapper.insert(any(Exam.class))).thenAnswer(invocation -> {
            Exam exam = invocation.getArgument(0);
            exam.setId(1L);
            return 1;
        });

        boolean result = examService.save(dto);
        assertTrue(result);
    }

    @Test
    @DisplayName("边界测试 - 长时间考试（30天）")
    void testSave_VeryLongExam() {
        ExamDTO dto = new ExamDTO();
        dto.setPaperId(1L);
        dto.setName("长时间考试");
        dto.setStartTime(LocalDateTime.now());
        dto.setEndTime(LocalDateTime.now().plusDays(30));

        when(examMapper.insert(any(Exam.class))).thenAnswer(invocation -> {
            Exam exam = invocation.getArgument(0);
            exam.setId(1L);
            return 1;
        });

        boolean result = examService.save(dto);
        assertTrue(result);
    }

    @Test
    @DisplayName("接口交互测试 - 保存考试时只调用examMapper")
    void testInterfaceInteraction_SaveExam() {
        when(examMapper.insert(any(Exam.class))).thenAnswer(invocation -> {
            Exam exam = invocation.getArgument(0);
            exam.setId(1L);
            return 1;
        });

        boolean result = examService.save(testExamDTO);

        assertTrue(result);
        verify(examMapper, times(1)).insert(any(Exam.class));
        verify(paperMapper, never()).insert(any(Paper.class));
        verify(paperMapper, never()).selectById(anyLong());
    }

    @Test
    @DisplayName("接口交互测试 - 获取详情时调用examMapper和paperMapper")
    void testInterfaceInteraction_GetDetail() {
        when(examMapper.selectById(1L)).thenReturn(testExam);
        when(paperMapper.selectById(1L)).thenReturn(testPaper);

        ExamVO result = examService.getDetail(1L);

        assertNotNull(result);
        verify(examMapper, times(1)).selectById(1L);
        verify(paperMapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("接口交互测试 - 分页查询时调用examMapper和paperMapper")
    void testInterfaceInteraction_PageQuery() {
        Page<Exam> examPage = new Page<>(1, 10);
        examPage.setRecords(Collections.singletonList(testExam));
        examPage.setTotal(1);

        when(examMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(examPage);
        when(paperMapper.selectBatchIds(anyList()))
                .thenReturn(Collections.singletonList(testPaper));

        IPage<ExamVO> result = examService.page(1, 10, null);

        assertNotNull(result);
        verify(examMapper, times(1)).selectPage(any(Page.class), any(LambdaQueryWrapper.class));
        verify(paperMapper, times(1)).selectBatchIds(anyList());
    }

    @Test
    @DisplayName("分页查询 - 状态筛选后总数更新")
    void testPage_StatusFilterUpdatesTotal() {
        Exam exam1 = new Exam();
        exam1.setId(1L);
        exam1.setName("考试1");
        exam1.setStartTime(LocalDateTime.now().plusDays(1));
        exam1.setEndTime(LocalDateTime.now().plusDays(2));
        exam1.setPaperId(1L);

        Exam exam2 = new Exam();
        exam2.setId(2L);
        exam2.setName("考试2");
        exam2.setStartTime(LocalDateTime.now().minusDays(2));
        exam2.setEndTime(LocalDateTime.now().minusDays(1));
        exam2.setPaperId(1L);

        List<Exam> exams = new ArrayList<>();
        exams.add(exam1);
        exams.add(exam2);

        Page<Exam> examPage = new Page<>(1, 10);
        examPage.setRecords(exams);
        examPage.setTotal(2);

        when(examMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(examPage);
        when(paperMapper.selectBatchIds(anyList()))
                .thenReturn(Collections.singletonList(testPaper));

        IPage<ExamVO> result = examService.page(1, 10, Constants.EXAM_NOT_START);

        assertEquals(1, result.getTotal());
    }

    @Test
    @DisplayName("状态计算 - 当前时间在开始时间之前")
    void testComputeStatus_BeforeStart() {
        Exam exam = new Exam();
        exam.setStartTime(LocalDateTime.now().plusHours(1));
        exam.setEndTime(LocalDateTime.now().plusHours(2));

        when(examMapper.selectById(1L)).thenReturn(exam);

        ExamVO result = examService.getDetail(1L);
        assertEquals(Constants.EXAM_NOT_START, result.getStatus());
    }

    @Test
    @DisplayName("状态计算 - 当前时间在结束时间之后")
    void testComputeStatus_AfterEnd() {
        Exam exam = new Exam();
        exam.setStartTime(LocalDateTime.now().minusHours(2));
        exam.setEndTime(LocalDateTime.now().minusHours(1));

        when(examMapper.selectById(1L)).thenReturn(exam);

        ExamVO result = examService.getDetail(1L);
        assertEquals(Constants.EXAM_END, result.getStatus());
    }

    @Test
    @DisplayName("状态计算 - 当前时间在考试期间")
    void testComputeStatus_DuringExam() {
        Exam exam = new Exam();
        exam.setStartTime(LocalDateTime.now().minusHours(1));
        exam.setEndTime(LocalDateTime.now().plusHours(1));

        when(examMapper.selectById(1L)).thenReturn(exam);

        ExamVO result = examService.getDetail(1L);
        assertEquals(Constants.EXAM_ING, result.getStatus());
    }
}
