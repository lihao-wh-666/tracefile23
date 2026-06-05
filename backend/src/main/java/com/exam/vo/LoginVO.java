package com.exam.vo;

import lombok.Data;

@Data
public class LoginVO {

    private String token;

    private Long userId;

    private String username;

    private String realName;

    private Integer role;

    private String avatar;
}
