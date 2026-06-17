package com.exam.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class VideoCategoryDTO {

    @NotBlank
    private String name;

    private String description;

    private Integer sort;

    private Integer status;
}
