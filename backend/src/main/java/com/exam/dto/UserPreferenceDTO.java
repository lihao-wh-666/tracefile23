package com.exam.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class UserPreferenceDTO {

    @NotBlank(message = "主题不能为空")
    private String theme;

    @NotBlank(message = "语言不能为空")
    private String language;

    private Integer sidebarCollapsed;

    private String extraConfig;
}
