package com.exam.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.exam.common.BusinessException;
import com.exam.common.ErrorCode;
import com.exam.dto.QuestionDTO;
import com.exam.entity.Question;
import com.exam.entity.Subject;
import com.exam.mapper.QuestionMapper;
import com.exam.mapper.SubjectMapper;
import com.exam.service.QuestionService;
import com.exam.vo.QuestionImportVO;
import com.exam.vo.QuestionVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuestionServiceImpl implements QuestionService {

    private static final Map<String, Integer> TYPE_MAP = new HashMap<>();
    private static final Map<String, Integer> DIFFICULTY_MAP = new HashMap<>();
    private static final List<String> TEMPLATE_HEADERS = Arrays.asList(
            "题型", "题目内容", "选项A", "选项B", "选项C", "选项D",
            "答案", "解析", "分值", "难度"
    );

    static {
        TYPE_MAP.put("单选", 1);
        TYPE_MAP.put("多选", 2);
        TYPE_MAP.put("判断", 3);
        TYPE_MAP.put("填空", 4);
        TYPE_MAP.put("问答", 5);

        DIFFICULTY_MAP.put("简单", 1);
        DIFFICULTY_MAP.put("中等", 2);
        DIFFICULTY_MAP.put("困难", 3);
    }

    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private SubjectMapper subjectMapper;

    @Override
    public IPage<QuestionVO> page(Integer current, Integer size, Long subjectId, Integer type, Integer difficulty) {
        Page<Question> page = new Page<>(current, size);
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (subjectId != null) {
            wrapper.eq(Question::getSubjectId, subjectId);
        }
        if (type != null) {
            wrapper.eq(Question::getType, type);
        }
        if (difficulty != null) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }
        wrapper.orderByDesc(Question::getCreateTime);
        IPage<Question> questionPage = questionMapper.selectPage(page, wrapper);

        IPage<QuestionVO> voPage = questionPage.convert(q -> {
            QuestionVO vo = new QuestionVO();
            BeanUtils.copyProperties(q, vo);
            return vo;
        });

        List<Long> subjectIds = voPage.getRecords().stream()
                .map(QuestionVO::getSubjectId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!subjectIds.isEmpty()) {
            List<Subject> subjects = subjectMapper.selectBatchIds(subjectIds);
            Map<Long, String> subjectNameMap = subjects.stream()
                    .collect(Collectors.toMap(Subject::getId, Subject::getName));
            voPage.getRecords().forEach(vo -> vo.setSubjectName(subjectNameMap.get(vo.getSubjectId())));
        }

        return voPage;
    }

    @Override
    public QuestionVO getDetail(Long id) {
        Question question = questionMapper.selectById(id);
        if (question == null) {
            return null;
        }
        QuestionVO vo = new QuestionVO();
        BeanUtils.copyProperties(question, vo);
        Subject subject = subjectMapper.selectById(question.getSubjectId());
        if (subject != null) {
            vo.setSubjectName(subject.getName());
        }
        return vo;
    }

    @Override
    public boolean save(QuestionDTO dto) {
        Question entity = new Question();
        BeanUtils.copyProperties(dto, entity);
        return questionMapper.insert(entity) > 0;
    }

    @Override
    public boolean update(Long id, QuestionDTO dto) {
        Question entity = new Question();
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        return questionMapper.updateById(entity) > 0;
    }

    @Override
    public boolean removeById(Long id) {
        return questionMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public QuestionImportVO importQuestions(MultipartFile file, Long subjectId) {
        QuestionImportVO result = new QuestionImportVO();

        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_EMPTY.getCode(), "请选择要导入的文件");
        }

        String filename = file.getOriginalFilename();
        if (StrUtil.isBlank(filename)) {
            throw new BusinessException(ErrorCode.FILE_TYPE_ERROR.getCode(), "文件名不能为空");
        }

        String lowerFilename = filename.toLowerCase();
        if (!lowerFilename.endsWith(".xlsx") && !lowerFilename.endsWith(".xls") && !lowerFilename.endsWith(".csv")) {
            throw new BusinessException(ErrorCode.FILE_TYPE_ERROR.getCode(), "仅支持Excel(.xlsx, .xls)和CSV格式文件");
        }

        if (subjectId == null) {
            throw new BusinessException(ErrorCode.PARAM_EMPTY.getCode(), "请选择所属科目");
        }
        Subject subject = subjectMapper.selectById(subjectId);
        if (subject == null) {
            throw new BusinessException(ErrorCode.SUBJECT_NOT_FOUND);
        }

        List<Map<String, Object>> rows;
        try (InputStream is = file.getInputStream()) {
            ExcelReader reader = ExcelUtil.getReader(is);
            rows = reader.readAll();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR.getCode(), "文件读取失败：" + e.getMessage());
        }

        if (CollUtil.isEmpty(rows)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "文件中没有数据");
        }

        List<Question> questionsToSave = new ArrayList<>();
        int rowNum = 1;

        for (Map<String, Object> row : rows) {
            rowNum++;
            try {
                Question question = parseRow(row, subjectId);
                questionsToSave.add(question);
            } catch (Exception e) {
                result.addError(rowNum, e.getMessage());
            }
        }

        result.setTotalCount(rows.size());
        result.setFailCount(result.getErrors().size());
        result.setSuccessCount(result.getTotalCount() - result.getFailCount());

        if (CollUtil.isNotEmpty(questionsToSave)) {
            for (Question question : questionsToSave) {
                questionMapper.insert(question);
            }
        }

        return result;
    }

    private Question parseRow(Map<String, Object> row, Long subjectId) {
        Question question = new Question();

        String typeStr = getStringValue(row.get("题型"));
        if (StrUtil.isBlank(typeStr)) {
            throw new IllegalArgumentException("题型不能为空");
        }
        Integer type = TYPE_MAP.get(typeStr.trim());
        if (type == null) {
            throw new IllegalArgumentException("题型格式不正确，支持：单选、多选、判断、填空、问答");
        }
        question.setType(type);

        String content = getStringValue(row.get("题目内容"));
        if (StrUtil.isBlank(content)) {
            throw new IllegalArgumentException("题目内容不能为空");
        }
        question.setContent(content.trim());

        String answer = getStringValue(row.get("答案"));
        if (StrUtil.isBlank(answer)) {
            throw new IllegalArgumentException("答案不能为空");
        }
        question.setAnswer(answer.trim());

        String scoreStr = getStringValue(row.get("分值"));
        if (StrUtil.isBlank(scoreStr)) {
            throw new IllegalArgumentException("分值不能为空");
        }
        try {
            int score = Integer.parseInt(scoreStr.trim());
            if (score <= 0 || score > 100) {
                throw new IllegalArgumentException("分值必须在1-100之间");
            }
            question.setScore(score);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("分值格式不正确，请输入数字");
        }

        String difficultyStr = getStringValue(row.get("难度"));
        if (StrUtil.isNotBlank(difficultyStr)) {
            Integer difficulty = DIFFICULTY_MAP.get(difficultyStr.trim());
            if (difficulty == null) {
                throw new IllegalArgumentException("难度格式不正确，支持：简单、中等、困难");
            }
            question.setDifficulty(difficulty);
        } else {
            question.setDifficulty(2);
        }

        question.setOptionA(getStringValue(row.get("选项A")));
        question.setOptionB(getStringValue(row.get("选项B")));
        question.setOptionC(getStringValue(row.get("选项C")));
        question.setOptionD(getStringValue(row.get("选项D")));
        question.setAnalysis(getStringValue(row.get("解析")));
        question.setSubjectId(subjectId);

        return question;
    }

    private String getStringValue(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        try (ExcelWriter writer = ExcelUtil.getWriter()) {
            writer.setColumnWidth(0, 10);
            writer.setColumnWidth(1, 40);
            writer.setColumnWidth(2, 25);
            writer.setColumnWidth(3, 25);
            writer.setColumnWidth(4, 25);
            writer.setColumnWidth(5, 25);
            writer.setColumnWidth(6, 20);
            writer.setColumnWidth(7, 30);
            writer.setColumnWidth(8, 8);
            writer.setColumnWidth(9, 8);

            writer.writeHeadRow(TEMPLATE_HEADERS);

            List<List<Object>> rows = new ArrayList<>();
            rows.add(Arrays.asList("单选", "1+1等于几？", "1", "2", "3", "4", "B", "基础算术题", 5, "简单"));
            rows.add(Arrays.asList("多选", "以下哪些是编程语言？", "Java", "Python", "HTML", "CSS", "AB", "编程语言分类", 10, "中等"));
            rows.add(Arrays.asList("判断", "地球是圆的。", "", "", "", "", "正确", "地理常识", 3, "简单"));
            rows.add(Arrays.asList("填空", "中国的首都是___。", "", "", "", "", "北京", "地理常识", 5, "简单"));
            rows.add(Arrays.asList("问答", "请简述Java的特点。", "", "", "", "", "面向对象、跨平台...", "Java基础", 15, "困难"));

            writer.write(rows);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("题目导入模板.xlsx", StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);

            ServletOutputStream out = response.getOutputStream();
            writer.flush(out, true);
            writer.close();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "模板下载失败：" + e.getMessage());
        }
    }
}
