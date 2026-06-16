package com.exam.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class ExamSwitchRecordDTO {

    @NotNull(message = "考试记录ID不能为空")
    private Long recordId;

    @NotNull(message = "切屏类型不能为空")
    private Integer switchType;

    private Integer duration;

    private String appName;

    private Integer screenshotDetected;

    private Integer screenRecordDetected;

    private String details;
}
