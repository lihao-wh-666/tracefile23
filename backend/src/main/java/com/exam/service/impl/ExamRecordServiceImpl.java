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
import com.exam.vo.PersonalScoreStatVO;
import com.exam.vo.ScoreStatVO;
import com.exam.vo.WrongQuestionVO;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
        Font headerFont = workbook.createFont();
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

        BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        Font titleFont = new Font(bfChinese, 18, Font.BOLD);
        Font headerFont = new Font(bfChinese, 12, Font.BOLD);
        Font normalFont = new Font(bfChinese, 10, Font.NORMAL);

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
            cell.setBackgroundColor(new com.lowagie.text.Color(240, 240, 240));
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

    private PdfPCell createPdfCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        return cell;
    }
}
