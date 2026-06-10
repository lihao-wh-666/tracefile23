package com.exam.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class SaveAnswersDTO {

    @NotNull
    private Long recordId;

    private List<SaveAnswerItem> answers;

    @Data
    public static class SaveAnswerItem {
        private Long questionId;
        private String answer;
    }
}
