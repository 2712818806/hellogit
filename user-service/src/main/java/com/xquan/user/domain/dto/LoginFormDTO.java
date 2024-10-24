package com.xquan.user.domain.dto;

import lombok.Data;

@Data
public class LoginFormDTO {
    private String username;
    private String password;
    private String phone;
    private String code;
}
