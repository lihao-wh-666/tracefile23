package com.exam.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class PaperDTO {

    @NotBlank
    private String name;

    @NotNull
    private Long subjectId;

    @NotNull
    private Integer totalScore;

    @NotNull
    private Integer passScore;

    @NotNull
    private Integer duration;

    private List<Long> questionIds;
}
