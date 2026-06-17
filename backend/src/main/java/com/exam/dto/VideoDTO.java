package com.exam.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class VideoDTO {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Long categoryId;

    private String coverUrl;

    @NotBlank
    private String videoUrl;

    private String duration;

    private Long fileSize;

    private String tags;

    private Integer status;
}
