package com.exam.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UserDTO {

    @NotBlank
    private String username;

    private String password;

    private String realName;

    @NotNull
    private Integer role;

    private String email;

    private String phone;
}
