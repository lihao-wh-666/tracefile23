package com.exam.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class QuestionDTO {

    private Long subjectId;

    @NotNull
    private Integer type;

    @NotBlank
    private String content;

    private String optionA;

    private String optionB;

    private String optionC;

    private String optionD;

    @NotBlank
    private String answer;

    private String analysis;

    @NotNull
    private Integer score;

    @NotNull
    private Integer difficulty;
}
