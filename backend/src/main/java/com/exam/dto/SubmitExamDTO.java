package com.exam.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SubmitExamDTO {

    @NotNull
    private Long examId;

    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {

        private Long questionId;

        private String answer;
    }
}
