package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.dto.SubmitExamDTO;
import com.exam.vo.ExamRecordVO;
import com.exam.vo.PersonalScoreStatVO;
import com.exam.vo.ScoreStatVO;
import com.exam.vo.WrongQuestionVO;

import java.util.List;

public interface ExamRecordService {

    IPage<ExamRecordVO> page(Integer current, Integer size, Long examId, Long userId, Integer status);

    ExamRecordVO getDetail(Long id);

    ExamRecordVO startExam(Long examId, Long userId);

    ExamRecordVO submitExam(SubmitExamDTO dto, Long userId);

    List<ScoreStatVO> scoreStats(Long examId);

    PersonalScoreStatVO getPersonalStat(Long userId);

    IPage<ExamRecordVO> getMyRecords(Integer current, Integer size, Long userId);

    IPage<WrongQuestionVO> getWrongQuestions(Integer current, Integer size, Long userId);
}
