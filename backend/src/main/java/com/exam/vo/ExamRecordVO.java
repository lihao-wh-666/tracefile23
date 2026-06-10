package com.exam.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExamRecordVO {

    private Long id;

    private Long examId;

    private String examName;

    private Long userId;

    private String userName;

    private String realName;

    private Long paperId;

    private LocalDateTime startTime;

    private LocalDateTime submitTime;

    private Integer score;

    private Integer totalScore;

    private Integer status;

    private Integer duration;

    private Integer rank;

    private Integer pauseCount;

    private Integer totalPauseTime;

    private LocalDateTime lastPauseTime;

    private LocalDateTime lastResumeTime;

    private LocalDateTime examEndTime;

    private Integer paperDuration;
}
