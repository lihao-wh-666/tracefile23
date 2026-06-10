package com.exam.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.Constants;
import com.exam.dto.PauseExamDTO;
import com.exam.dto.SaveAnswerDTO;
import com.exam.dto.SaveAnswersDTO;
import com.exam.dto.SubmitExamDTO;
import com.exam.entity.*;
import com.exam.mapper.*;
import com.exam.service.ExamRecordService;
import com.exam.vo.ExamRecordVO;
import com.exam.vo.PersonalScoreStatVO;
import com.exam.vo.ScoreStatVO;
import com.exam.vo.WrongQuestionVO;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
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
            if (existing.getStatus() == Constants.RECORD_EXAMING || existing.getStatus() == Constants.RECORD_PAUSED) {
                ExamRecordVO vo = buildRecordVO(existing);
                fillBatchInfo(Collections.singletonList(vo));
                return vo;
            }
            throw new RuntimeException("您已参加过该考试");
        }

        Paper paper = paperMapper.selectById(exam.getPaperId());

        ExamRecord record = new ExamRecord();
        record.setExamId(examId);
        record.setUserId(userId);
        record.setPaperId(exam.getPaperId());
        record.setStartTime(now);
        record.setStatus(Constants.RECORD_EXAMING);
        record.setPauseCount(0);
        record.setTotalPauseTime(0);
        record.setDuration(paper != null ? paper.getDuration() * 60 : 0);
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
                        .in(ExamRecord::getStatus, Constants.RECORD_EXAMING, Constants.RECORD_PAUSED));
        if (record == null) {
            throw new RuntimeException("考试记录不存在");
        }

        Exam exam = examMapper.selectById(record.getExamId());
        LocalDateTime now = LocalDateTime.now();
        boolean isTimeout = now.isAfter(exam.getEndTime());

        record.setSubmitTime(now);
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

    @Override
    public PersonalScoreStatVO getPersonalStat(Long userId) {
        List<ExamRecord> records = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getUserId, userId)
                        .in(ExamRecord::getStatus, Constants.RECORD_SUBMITTED, Constants.RECORD_GRADED)
                        .orderByDesc(ExamRecord::getSubmitTime));

        PersonalScoreStatVO stat = new PersonalScoreStatVO();
        stat.setTotalExamCount(records.size());
        stat.setSubmittedCount(records.size());

        if (records.isEmpty()) {
            stat.setMaxScore(0);
            stat.setAvgScore(0.0);
            stat.setAccuracyRate(0.0);
            stat.setTotalQuestionCount(0);
            stat.setCorrectQuestionCount(0);
            stat.setWrongQuestionCount(0);
            stat.setTotalScore(0);
            stat.setTotalFullScore(0);
            return stat;
        }

        int maxScore = records.stream().mapToInt(ExamRecord::getScore).max().orElse(0);
        double avgScore = records.stream().mapToInt(ExamRecord::getScore).average().orElse(0.0);
        stat.setMaxScore(maxScore);
        stat.setAvgScore(Math.round(avgScore * 100.0) / 100.0);

        ExamRecord maxRecord = records.stream()
                .filter(r -> r.getScore() != null && r.getScore() == maxScore)
                .findFirst().orElse(null);
        if (maxRecord != null) {
            stat.setMaxScoreExamId(maxRecord.getExamId());
            Exam exam = examMapper.selectById(maxRecord.getExamId());
            if (exam != null) {
                stat.setMaxScoreExamName(exam.getName());
            }
        }

        int totalScore = records.stream().mapToInt(ExamRecord::getScore).sum();
        stat.setTotalScore(totalScore);

        List<Long> recordIds = records.stream().map(ExamRecord::getId).collect(Collectors.toList());
        List<ExamAnswer> answers = examAnswerMapper.selectList(
                new LambdaQueryWrapper<ExamAnswer>().in(ExamAnswer::getRecordId, recordIds));

        int totalQuestionCount = answers.size();
        int correctQuestionCount = (int) answers.stream().filter(a -> a.getIsCorrect() != null && a.getIsCorrect() == 1).count();
        int wrongQuestionCount = (int) answers.stream().filter(a -> a.getIsCorrect() != null && a.getIsCorrect() == 0).count();

        stat.setTotalQuestionCount(totalQuestionCount);
        stat.setCorrectQuestionCount(correctQuestionCount);
        stat.setWrongQuestionCount(wrongQuestionCount);

        if (totalQuestionCount > 0) {
            double accuracyRate = (double) correctQuestionCount / totalQuestionCount * 100;
            stat.setAccuracyRate(Math.round(accuracyRate * 100.0) / 100.0);
        } else {
            stat.setAccuracyRate(0.0);
        }

        List<Long> paperIds = records.stream().map(ExamRecord::getPaperId).distinct().collect(Collectors.toList());
        int totalFullScore = 0;
        if (!paperIds.isEmpty()) {
            List<Paper> papers = paperMapper.selectBatchIds(paperIds);
            Map<Long, Paper> paperMap = papers.stream().collect(Collectors.toMap(Paper::getId, p -> p));
            for (ExamRecord record : records) {
                Paper paper = paperMap.get(record.getPaperId());
                if (paper != null) {
                    totalFullScore += paper.getTotalScore();
                }
            }
        }
        stat.setTotalFullScore(totalFullScore);

        return stat;
    }

    @Override
    public IPage<ExamRecordVO> getMyRecords(Integer current, Integer size, Long userId) {
        Page<ExamRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getUserId, userId);
        wrapper.orderByDesc(ExamRecord::getSubmitTime);
        IPage<ExamRecord> recordPage = examRecordMapper.selectPage(page, wrapper);

        IPage<ExamRecordVO> voPage = recordPage.convert(r -> buildRecordVO(r));
        fillBatchInfo(voPage.getRecords());
        fillRankingInfo(voPage.getRecords());

        return voPage;
    }

    @Override
    public IPage<WrongQuestionVO> getWrongQuestions(Integer current, Integer size, Long userId) {
        List<ExamRecord> records = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getUserId, userId)
                        .in(ExamRecord::getStatus, Constants.RECORD_SUBMITTED, Constants.RECORD_GRADED));

        if (records.isEmpty()) {
            return new Page<>(current, size, 0);
        }

        List<Long> recordIds = records.stream().map(ExamRecord::getId).collect(Collectors.toList());
        Map<Long, ExamRecord> recordMap = records.stream().collect(Collectors.toMap(ExamRecord::getId, r -> r));

        List<ExamAnswer> wrongAnswers = examAnswerMapper.selectList(
                new LambdaQueryWrapper<ExamAnswer>()
                        .in(ExamAnswer::getRecordId, recordIds)
                        .eq(ExamAnswer::getIsCorrect, 0));

        int total = wrongAnswers.size();
        int start = (current - 1) * size;
        int end = Math.min(start + size, total);
        List<ExamAnswer> pageAnswers = start >= total ? Collections.emptyList() : wrongAnswers.subList(start, end);

        List<Long> questionIds = pageAnswers.stream().map(ExamAnswer::getQuestionId).collect(Collectors.toList());
        Map<Long, Question> questionMap = new HashMap<>();
        if (!questionIds.isEmpty()) {
            questionMapper.selectBatchIds(questionIds).forEach(q -> questionMap.put(q.getId(), q));
        }

        List<Long> examIds = records.stream().map(ExamRecord::getExamId).distinct().collect(Collectors.toList());
        Map<Long, Exam> examMap = new HashMap<>();
        if (!examIds.isEmpty()) {
            examMapper.selectBatchIds(examIds).forEach(e -> examMap.put(e.getId(), e));
        }

        List<WrongQuestionVO> voList = new ArrayList<>();
        for (ExamAnswer answer : pageAnswers) {
            Question question = questionMap.get(answer.getQuestionId());
            ExamRecord record = recordMap.get(answer.getRecordId());
            if (question == null || record == null) {
                continue;
            }

            WrongQuestionVO vo = new WrongQuestionVO();
            vo.setQuestionId(question.getId());
            vo.setExamId(record.getExamId());
            Exam exam = examMap.get(record.getExamId());
            if (exam != null) {
                vo.setExamName(exam.getName());
            }
            vo.setRecordId(record.getId());
            vo.setType(question.getType());
            vo.setContent(question.getContent());
            vo.setOptionA(question.getOptionA());
            vo.setOptionB(question.getOptionB());
            vo.setOptionC(question.getOptionC());
            vo.setOptionD(question.getOptionD());
            vo.setUserAnswer(answer.getAnswer());
            vo.setCorrectAnswer(question.getAnswer());
            vo.setScore(answer.getScore());
            vo.setTotalScore(question.getScore());
            vo.setAnalysis(question.getAnalysis());
            vo.setSubmitTime(record.getSubmitTime());
            voList.add(vo);
        }

        Page<WrongQuestionVO> resultPage = new Page<>(current, size, total);
        resultPage.setRecords(voList);
        return resultPage;
    }

    private void fillRankingInfo(List<ExamRecordVO> records) {
        if (records.isEmpty()) {
            return;
        }
        for (ExamRecordVO vo : records) {
            if (vo.getExamId() == null) {
                continue;
            }
            List<ExamRecord> allRecords = examRecordMapper.selectList(
                    new LambdaQueryWrapper<ExamRecord>()
                            .eq(ExamRecord::getExamId, vo.getExamId())
                            .in(ExamRecord::getStatus, Constants.RECORD_SUBMITTED, Constants.RECORD_GRADED)
                            .orderByDesc(ExamRecord::getScore));

            int rank = 0;
            int prevScore = -1;
            for (int i = 0; i < allRecords.size(); i++) {
                ExamRecord r = allRecords.get(i);
                if (r.getScore() == null || !r.getScore().equals(prevScore)) {
                    rank = i + 1;
                    prevScore = r.getScore();
                }
                if (r.getUserId().equals(vo.getUserId())) {
                    vo.setRank(rank);
                    break;
                }
            }
        }
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
                vo.setExamEndTime(exam.getEndTime());
            }
            User user = userMap.get(vo.getUserId());
            if (user != null) {
                vo.setUserName(user.getUsername());
                vo.setRealName(user.getRealName());
            }
            Paper paper = paperMap.get(vo.getPaperId());
            if (paper != null) {
                vo.setTotalScore(paper.getTotalScore());
                vo.setPaperDuration(paper.getDuration());
            }
        }
    }

    private List<ExamRecordVO> getRecordsForExport(Long examId) {
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        if (examId != null) {
            wrapper.eq(ExamRecord::getExamId, examId);
        }
        wrapper.in(ExamRecord::getStatus, Constants.RECORD_SUBMITTED, Constants.RECORD_GRADED);
        wrapper.orderByDesc(ExamRecord::getSubmitTime);
        List<ExamRecord> records = examRecordMapper.selectList(wrapper);

        List<ExamRecordVO> voList = records.stream()
                .map(this::buildRecordVO)
                .collect(Collectors.toList());
        fillBatchInfo(voList);
        return voList;
    }

    @Override
    public byte[] exportExcel(Long examId) throws Exception {
        List<ExamRecordVO> records = getRecordsForExport(examId);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("成绩统计");

        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        String[] headers = {"序号", "考试名称", "学生姓名", "用户名", "分数", "总分", "状态", "提交时间", "用时(分钟)"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (ExamRecordVO record : records) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rowNum - 1);
            row.createCell(1).setCellValue(record.getExamName() != null ? record.getExamName() : "");
            row.createCell(2).setCellValue(record.getRealName() != null ? record.getRealName() : "");
            row.createCell(3).setCellValue(record.getUserName() != null ? record.getUserName() : "");
            row.createCell(4).setCellValue(record.getScore() != null ? record.getScore() : 0);
            row.createCell(5).setCellValue(record.getTotalScore() != null ? record.getTotalScore() : 0);
            row.createCell(6).setCellValue(getStatusText(record.getStatus()));
            row.createCell(7).setCellValue(record.getSubmitTime() != null ? record.getSubmitTime().toString() : "");
            row.createCell(8).setCellValue(record.getDuration() != null ? record.getDuration() : 0);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    @Override
    public byte[] exportCsv(Long examId) throws Exception {
        List<ExamRecordVO> records = getRecordsForExport(examId);

        StringBuilder sb = new StringBuilder();
        sb.append("序号,考试名称,学生姓名,用户名,分数,总分,状态,提交时间,用时(分钟)\n");

        int rowNum = 1;
        for (ExamRecordVO record : records) {
            sb.append(rowNum++).append(",");
            sb.append(escapeCsv(record.getExamName())).append(",");
            sb.append(escapeCsv(record.getRealName())).append(",");
            sb.append(escapeCsv(record.getUserName())).append(",");
            sb.append(record.getScore() != null ? record.getScore() : 0).append(",");
            sb.append(record.getTotalScore() != null ? record.getTotalScore() : 0).append(",");
            sb.append(escapeCsv(getStatusText(record.getStatus()))).append(",");
            sb.append(record.getSubmitTime() != null ? record.getSubmitTime().toString() : "").append(",");
            sb.append(record.getDuration() != null ? record.getDuration() : 0).append("\n");
        }

        return sb.toString().getBytes("UTF-8");
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case Constants.RECORD_EXAMING:
                return "考试中";
            case Constants.RECORD_SUBMITTED:
                return "已提交";
            case Constants.RECORD_GRADED:
                return "已阅卷";
            case Constants.RECORD_PAUSED:
                return "已暂停";
            default:
                return "未知";
        }
    }

    @Override
    public ExamRecordVO getCurrentExam(Long userId) {
        ExamRecord record = examRecordMapper.selectOne(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getUserId, userId)
                        .in(ExamRecord::getStatus, Constants.RECORD_EXAMING, Constants.RECORD_PAUSED)
                        .orderByDesc(ExamRecord::getStartTime)
                        .last("LIMIT 1"));
        if (record == null) {
            return null;
        }
        ExamRecordVO vo = buildRecordVO(record);
        fillBatchInfo(Collections.singletonList(vo));
        return vo;
    }

    @Override
    public boolean saveAnswer(SaveAnswerDTO dto, Long userId) {
        ExamRecord record = examRecordMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new RuntimeException("考试记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此考试记录");
        }
        if (record.getStatus() != Constants.RECORD_EXAMING && record.getStatus() != Constants.RECORD_PAUSED) {
            throw new RuntimeException("考试已提交，无法修改答案");
        }

        ExamAnswer answer = examAnswerMapper.selectOne(
                new LambdaQueryWrapper<ExamAnswer>()
                        .eq(ExamAnswer::getRecordId, dto.getRecordId())
                        .eq(ExamAnswer::getQuestionId, dto.getQuestionId()));
        if (answer == null) {
            throw new RuntimeException("答题记录不存在");
        }
        answer.setAnswer(dto.getAnswer() != null ? dto.getAnswer() : "");
        examAnswerMapper.updateById(answer);
        return true;
    }

    @Override
    public boolean saveAnswers(SaveAnswersDTO dto, Long userId) {
        ExamRecord record = examRecordMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new RuntimeException("考试记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此考试记录");
        }
        if (record.getStatus() != Constants.RECORD_EXAMING && record.getStatus() != Constants.RECORD_PAUSED) {
            throw new RuntimeException("考试已提交，无法修改答案");
        }

        if (dto.getAnswers() != null && !dto.getAnswers().isEmpty()) {
            List<ExamAnswer> existingAnswers = examAnswerMapper.selectList(
                    new LambdaQueryWrapper<ExamAnswer>().eq(ExamAnswer::getRecordId, dto.getRecordId()));
            Map<Long, ExamAnswer> answerMap = existingAnswers.stream()
                    .collect(Collectors.toMap(ExamAnswer::getQuestionId, a -> a));

            for (SaveAnswersDTO.SaveAnswerItem item : dto.getAnswers()) {
                ExamAnswer answer = answerMap.get(item.getQuestionId());
                if (answer != null) {
                    answer.setAnswer(item.getAnswer() != null ? item.getAnswer() : "");
                    examAnswerMapper.updateById(answer);
                }
            }
        }
        return true;
    }

    @Override
    public ExamRecordVO pauseExam(PauseExamDTO dto, Long userId) {
        ExamRecord record = examRecordMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new RuntimeException("考试记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此考试记录");
        }
        if (record.getStatus() != Constants.RECORD_EXAMING) {
            throw new RuntimeException("当前状态无法暂停考试");
        }

        Exam exam = examMapper.selectById(record.getExamId());
        if (exam != null && LocalDateTime.now().isAfter(exam.getEndTime())) {
            throw new RuntimeException("考试已结束，无法暂停");
        }

        record.setStatus(Constants.RECORD_PAUSED);
        record.setLastPauseTime(LocalDateTime.now());
        record.setPauseCount(record.getPauseCount() != null ? record.getPauseCount() + 1 : 1);
        examRecordMapper.updateById(record);

        ExamRecordVO vo = buildRecordVO(record);
        fillBatchInfo(Collections.singletonList(vo));
        return vo;
    }

    @Override
    public ExamRecordVO resumeExam(PauseExamDTO dto, Long userId) {
        ExamRecord record = examRecordMapper.selectById(dto.getRecordId());
        if (record == null) {
            throw new RuntimeException("考试记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此考试记录");
        }
        if (record.getStatus() != Constants.RECORD_PAUSED) {
            throw new RuntimeException("当前状态无法恢复考试");
        }

        Exam exam = examMapper.selectById(record.getExamId());
        if (exam != null && LocalDateTime.now().isAfter(exam.getEndTime())) {
            throw new RuntimeException("考试已结束，无法恢复");
        }

        LocalDateTime now = LocalDateTime.now();
        if (record.getLastPauseTime() != null) {
            long pauseSeconds = java.time.Duration.between(record.getLastPauseTime(), now).getSeconds();
            record.setTotalPauseTime(record.getTotalPauseTime() != null
                    ? record.getTotalPauseTime() + (int) pauseSeconds
                    : (int) pauseSeconds);
        }

        record.setStatus(Constants.RECORD_EXAMING);
        record.setLastResumeTime(now);
        examRecordMapper.updateById(record);

        ExamRecordVO vo = buildRecordVO(record);
        fillBatchInfo(Collections.singletonList(vo));
        return vo;
    }

    @Override
    public List<ExamAnswer> getRecordAnswers(Long recordId, Long userId) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("考试记录不存在");
        }
        if (!record.getUserId().equals(userId)) {
            throw new RuntimeException("无权查看此考试记录");
        }
        return examAnswerMapper.selectList(
                new LambdaQueryWrapper<ExamAnswer>().eq(ExamAnswer::getRecordId, recordId));
    }

    private List<ExamRecordVO> getMyRecordsForExport(Long userId) {
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getUserId, userId);
        wrapper.in(ExamRecord::getStatus, Constants.RECORD_SUBMITTED, Constants.RECORD_GRADED);
        wrapper.orderByDesc(ExamRecord::getSubmitTime);
        List<ExamRecord> records = examRecordMapper.selectList(wrapper);

        List<ExamRecordVO> voList = records.stream()
                .map(this::buildRecordVO)
                .collect(Collectors.toList());
        fillBatchInfo(voList);
        fillRankingInfo(voList);
        return voList;
    }

    private List<WrongQuestionVO> getMyWrongQuestionsForExport(Long userId) {
        List<ExamRecord> records = examRecordMapper.selectList(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getUserId, userId)
                        .in(ExamRecord::getStatus, Constants.RECORD_SUBMITTED, Constants.RECORD_GRADED));

        if (records.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> recordIds = records.stream().map(ExamRecord::getId).collect(Collectors.toList());
        Map<Long, ExamRecord> recordMap = records.stream().collect(Collectors.toMap(ExamRecord::getId, r -> r));

        List<ExamAnswer> wrongAnswers = examAnswerMapper.selectList(
                new LambdaQueryWrapper<ExamAnswer>()
                        .in(ExamAnswer::getRecordId, recordIds)
                        .eq(ExamAnswer::getIsCorrect, 0));

        List<Long> questionIds = wrongAnswers.stream().map(ExamAnswer::getQuestionId).distinct().collect(Collectors.toList());
        Map<Long, Question> questionMap = new HashMap<>();
        if (!questionIds.isEmpty()) {
            questionMapper.selectBatchIds(questionIds).forEach(q -> questionMap.put(q.getId(), q));
        }

        List<Long> examIds = records.stream().map(ExamRecord::getExamId).distinct().collect(Collectors.toList());
        Map<Long, Exam> examMap = new HashMap<>();
        if (!examIds.isEmpty()) {
            examMapper.selectBatchIds(examIds).forEach(e -> examMap.put(e.getId(), e));
        }

        List<WrongQuestionVO> voList = new ArrayList<>();
        for (ExamAnswer answer : wrongAnswers) {
            Question question = questionMap.get(answer.getQuestionId());
            ExamRecord record = recordMap.get(answer.getRecordId());
            if (question == null || record == null) {
                continue;
            }

            WrongQuestionVO vo = new WrongQuestionVO();
            vo.setQuestionId(question.getId());
            vo.setExamId(record.getExamId());
            Exam exam = examMap.get(record.getExamId());
            if (exam != null) {
                vo.setExamName(exam.getName());
            }
            vo.setRecordId(record.getId());
            vo.setType(question.getType());
            vo.setContent(question.getContent());
            vo.setOptionA(question.getOptionA());
            vo.setOptionB(question.getOptionB());
            vo.setOptionC(question.getOptionC());
            vo.setOptionD(question.getOptionD());
            vo.setUserAnswer(answer.getAnswer());
            vo.setCorrectAnswer(question.getAnswer());
            vo.setScore(answer.getScore());
            vo.setTotalScore(question.getScore());
            vo.setAnalysis(question.getAnalysis());
            vo.setSubmitTime(record.getSubmitTime());
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public byte[] exportMyRecordsExcel(Long userId) throws Exception {
        List<ExamRecordVO> records = getMyRecordsForExport(userId);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("我的考试成绩");

        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        String[] headers = {"序号", "考试名称", "得分", "总分", "排名", "状态", "提交时间", "用时(分钟)"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (ExamRecordVO record : records) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rowNum - 1);
            row.createCell(1).setCellValue(record.getExamName() != null ? record.getExamName() : "");
            row.createCell(2).setCellValue(record.getScore() != null ? record.getScore() : 0);
            row.createCell(3).setCellValue(record.getTotalScore() != null ? record.getTotalScore() : 0);
            row.createCell(4).setCellValue(record.getRank() != null ? "第" + record.getRank() + "名" : "-");
            row.createCell(5).setCellValue(getStatusText(record.getStatus()));
            row.createCell(6).setCellValue(record.getSubmitTime() != null ? record.getSubmitTime().toString() : "");
            row.createCell(7).setCellValue(record.getDuration() != null ? record.getDuration() / 60 : 0);
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    @Override
    public byte[] exportMyRecordsCsv(Long userId) throws Exception {
        List<ExamRecordVO> records = getMyRecordsForExport(userId);

        StringBuilder sb = new StringBuilder();
        sb.append("序号,考试名称,得分,总分,排名,状态,提交时间,用时(分钟)\n");

        int rowNum = 1;
        for (ExamRecordVO record : records) {
            sb.append(rowNum++).append(",");
            sb.append(escapeCsv(record.getExamName())).append(",");
            sb.append(record.getScore() != null ? record.getScore() : 0).append(",");
            sb.append(record.getTotalScore() != null ? record.getTotalScore() : 0).append(",");
            sb.append(escapeCsv(record.getRank() != null ? "第" + record.getRank() + "名" : "-")).append(",");
            sb.append(escapeCsv(getStatusText(record.getStatus()))).append(",");
            sb.append(record.getSubmitTime() != null ? record.getSubmitTime().toString() : "").append(",");
            sb.append(record.getDuration() != null ? record.getDuration() / 60 : 0).append("\n");
        }

        return sb.toString().getBytes("UTF-8");
    }

    @Override
    public byte[] exportMyWrongQuestionsExcel(Long userId) throws Exception {
        List<WrongQuestionVO> wrongList = getMyWrongQuestionsForExport(userId);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("我的错题明细");

        CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        String[] headers = {"序号", "来源考试", "题型", "题目内容", "你的答案", "正确答案", "得分", "总分", "做错时间"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (WrongQuestionVO item : wrongList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rowNum - 1);
            row.createCell(1).setCellValue(item.getExamName() != null ? item.getExamName() : "");
            row.createCell(2).setCellValue(getQuestionTypeText(item.getType()));
            row.createCell(3).setCellValue(item.getContent() != null ? item.getContent() : "");
            row.createCell(4).setCellValue(item.getUserAnswer() != null ? item.getUserAnswer() : "-");
            row.createCell(5).setCellValue(item.getCorrectAnswer() != null ? item.getCorrectAnswer() : "-");
            row.createCell(6).setCellValue(item.getScore() != null ? item.getScore() : 0);
            row.createCell(7).setCellValue(item.getTotalScore() != null ? item.getTotalScore() : 0);
            row.createCell(8).setCellValue(item.getSubmitTime() != null ? item.getSubmitTime().toString() : "");
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream.toByteArray();
    }

    @Override
    public byte[] exportMyWrongQuestionsCsv(Long userId) throws Exception {
        List<WrongQuestionVO> wrongList = getMyWrongQuestionsForExport(userId);

        StringBuilder sb = new StringBuilder();
        sb.append("序号,来源考试,题型,题目内容,你的答案,正确答案,得分,总分,做错时间\n");

        int rowNum = 1;
        for (WrongQuestionVO item : wrongList) {
            sb.append(rowNum++).append(",");
            sb.append(escapeCsv(item.getExamName())).append(",");
            sb.append(escapeCsv(getQuestionTypeText(item.getType()))).append(",");
            sb.append(escapeCsv(item.getContent())).append(",");
            sb.append(escapeCsv(item.getUserAnswer() != null ? item.getUserAnswer() : "-")).append(",");
            sb.append(escapeCsv(item.getCorrectAnswer() != null ? item.getCorrectAnswer() : "-")).append(",");
            sb.append(item.getScore() != null ? item.getScore() : 0).append(",");
            sb.append(item.getTotalScore() != null ? item.getTotalScore() : 0).append(",");
            sb.append(item.getSubmitTime() != null ? item.getSubmitTime().toString() : "").append("\n");
        }

        return sb.toString().getBytes("UTF-8");
    }

    private String getQuestionTypeText(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case Constants.TYPE_SINGLE:
                return "单选题";
            case Constants.TYPE_MULTI:
                return "多选题";
            case Constants.TYPE_JUDGE:
                return "判断题";
            case Constants.TYPE_FILL:
                return "填空题";
            case Constants.TYPE_ESSAY:
                return "问答题";
            default:
                return "未知";
        }
    }

    @Override
    public byte[] exportPdf(Long examId) throws Exception {
        List<ExamRecordVO> records = getRecordsForExport(examId);
        ScoreStatVO stat = null;
        if (examId != null) {
            List<ScoreStatVO> stats = scoreStats(examId);
            if (!stats.isEmpty()) {
                stat = stats.get(0);
            }
        }

        Document document = new Document(PageSize.A4.rotate());
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        PdfWriter.getInstance(document, outputStream);
        document.open();

        BaseFont baseFont = getChineseFont();
        com.lowagie.text.Font titleFont = new com.lowagie.text.Font(baseFont, 18, com.lowagie.text.Font.BOLD);
        com.lowagie.text.Font headerFont = new com.lowagie.text.Font(baseFont, 12, com.lowagie.text.Font.BOLD);
        com.lowagie.text.Font normalFont = new com.lowagie.text.Font(baseFont, 10, com.lowagie.text.Font.NORMAL);

        String title = "成绩统计报表";
        if (stat != null && stat.getExamName() != null) {
            title = stat.getExamName() + " - 成绩统计报表";
        }
        Paragraph titlePara = new Paragraph(title, titleFont);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        titlePara.setSpacingAfter(20);
        document.add(titlePara);

        if (stat != null) {
            PdfPTable statTable = new PdfPTable(6);
            statTable.setWidthPercentage(100);
            statTable.setSpacingAfter(20);

            String[] statLabels = {"参考人数", "平均分", "最高分", "最低分", "及格人数", "及格率"};
            String[] statValues = {
                    String.valueOf(stat.getTotalCount()),
                    String.format("%.2f", stat.getAvgScore()),
                    String.valueOf(stat.getMaxScore()),
                    String.valueOf(stat.getMinScore()),
                    String.valueOf(stat.getPassCount()),
                    String.format("%.2f%%", stat.getPassRate())
            };

            for (int i = 0; i < statLabels.length; i++) {
                PdfPCell labelCell = new PdfPCell(new Phrase(statLabels[i], headerFont));
                labelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                labelCell.setPadding(8);
                statTable.addCell(labelCell);
            }
            for (int i = 0; i < statValues.length; i++) {
                PdfPCell valueCell = new PdfPCell(new Phrase(statValues[i], normalFont));
                valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                valueCell.setPadding(8);
                statTable.addCell(valueCell);
            }
            document.add(statTable);
        }

        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);

        String[] headers = {"序号", "学生姓名", "用户名", "分数", "总分", "状态", "提交时间", "用时(分钟)"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            cell.setBackgroundColor(new Color(240, 240, 240));
            table.addCell(cell);
        }

        int rowNum = 1;
        for (ExamRecordVO record : records) {
            table.addCell(createPdfCell(String.valueOf(rowNum++), normalFont));
            table.addCell(createPdfCell(record.getRealName() != null ? record.getRealName() : "", normalFont));
            table.addCell(createPdfCell(record.getUserName() != null ? record.getUserName() : "", normalFont));
            table.addCell(createPdfCell(String.valueOf(record.getScore() != null ? record.getScore() : 0), normalFont));
            table.addCell(createPdfCell(String.valueOf(record.getTotalScore() != null ? record.getTotalScore() : 0), normalFont));
            table.addCell(createPdfCell(getStatusText(record.getStatus()), normalFont));
            table.addCell(createPdfCell(record.getSubmitTime() != null ? record.getSubmitTime().toString() : "", normalFont));
            table.addCell(createPdfCell(String.valueOf(record.getDuration() != null ? record.getDuration() : 0), normalFont));
        }

        document.add(table);
        document.close();
        return outputStream.toByteArray();
    }

    private BaseFont getChineseFont() {
        String[] fontNames = {
            "/usr/share/fonts/truetype/wqy/wqy-microhei.ttc",
            "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc",
            "/usr/share/fonts/wqy-microhei/wqy-microhei.ttc",
            "/usr/share/fonts/wqy-zenhei/wqy-zenhei.ttc",
            "STSong-Light"
        };
        for (String fontName : fontNames) {
            try {
                if (fontName.startsWith("/")) {
                    return BaseFont.createFont(fontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                } else {
                    return BaseFont.createFont(fontName, "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
                }
            } catch (Exception ignored) {
            }
        }
        try {
            return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.EMBEDDED);
        } catch (Exception ex) {
            throw new RuntimeException("无法创建PDF字体", ex);
        }
    }

    private PdfPCell createPdfCell(String text, com.lowagie.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        return cell;
    }
}
