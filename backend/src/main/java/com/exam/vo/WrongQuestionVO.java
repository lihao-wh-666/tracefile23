package com.exam.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WrongQuestionVO {

    private Long questionId;

    private Long examId;

    private String examName;

    private Long recordId;

    private Integer type;

    private String content;

    private String optionA;

    private String optionB;

    private String optionC;

    private String optionD;

    private String userAnswer;

    private String correctAnswer;

    private Integer score;

    private Integer totalScore;

    private String analysis;

    private LocalDateTime submitTime;
}
