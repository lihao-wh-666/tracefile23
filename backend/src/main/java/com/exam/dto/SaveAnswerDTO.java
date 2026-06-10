package com.exam.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SaveAnswerDTO {

    @NotNull
    private Long recordId;

    @NotNull
    private Long questionId;

    private String answer;
}
