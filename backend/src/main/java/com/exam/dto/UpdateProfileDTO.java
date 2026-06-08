package com.exam.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

@Data
public class UpdateProfileDTO {

    private String realName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的手机号")
    private String phone;

    @Email(message = "请输入正确的邮箱地址")
    private String email;
}
