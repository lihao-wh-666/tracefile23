package com.exam.vo;

import lombok.Data;

@Data
public class ExamQuestionVO {

    private Long id;

    private Integer type;

    private String content;

    private String optionA;

    private String optionB;

    private String optionC;

    private String optionD;

    private Integer score;

    private String optionOrder;

    private Integer sortIndex;
}
