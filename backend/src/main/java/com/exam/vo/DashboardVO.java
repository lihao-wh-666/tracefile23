package com.exam.vo;

import lombok.Data;

import java.util.List;

@Data
public class DashboardVO {

    private Long subjectCount;

    private Long questionCount;

    private Long paperCount;

    private Long examCount;

    private Long userCount;

    private List<ExamVO> recentExams;
}
