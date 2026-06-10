package com.exam.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PauseExamDTO {

    @NotNull
    private Long recordId;
}
