package com.exam.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.exam.dto.PauseExamDTO;
import com.exam.dto.SaveAnswerDTO;
import com.exam.dto.SaveAnswersDTO;
import com.exam.dto.SubmitExamDTO;
import com.exam.entity.ExamAnswer;
import com.exam.vo.ExamQuestionVO;
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

    byte[] exportExcel(Long examId) throws Exception;

    byte[] exportCsv(Long examId) throws Exception;

    byte[] exportPdf(Long examId) throws Exception;

    ExamRecordVO getCurrentExam(Long userId);

    boolean saveAnswer(SaveAnswerDTO dto, Long userId);

    boolean saveAnswers(SaveAnswersDTO dto, Long userId);

    ExamRecordVO pauseExam(PauseExamDTO dto, Long userId);

    ExamRecordVO resumeExam(PauseExamDTO dto, Long userId);

    List<ExamAnswer> getRecordAnswers(Long recordId, Long userId);

    List<ExamQuestionVO> getExamQuestions(Long recordId, Long userId);

    byte[] exportMyRecordsExcel(Long userId) throws Exception;

    byte[] exportMyRecordsCsv(Long userId) throws Exception;

    byte[] exportMyWrongQuestionsExcel(Long userId) throws Exception;

    byte[] exportMyWrongQuestionsCsv(Long userId) throws Exception;
}
