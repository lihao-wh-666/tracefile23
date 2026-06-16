package com.exam.vo;

import lombok.Data;

@Data
public class ExamSwitchStatisticsVO {

    private Integer switchCount;

    private Integer totalSwitchDuration;

    private Integer screenshotCount;

    private Integer screenRecordCount;

    private Integer warningCount;

    private Integer maxSwitchCount;

    private Integer maxSingleSwitchDuration;

    private Integer maxTotalSwitchDuration;

    private Boolean screenshotDetectionEnabled;

    private Boolean screenRecordDetectionEnabled;

    private Boolean autoSubmitOnExceed;
}
