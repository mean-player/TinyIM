package com.example.demo.DTO;

import lombok.Data;

@Data
public class LoginRequest {
    private LoginType loginType;

    //密码+账号方式登录
    private String account;
    private String password;

    //邮箱+验证码方式登录
    private String email;
    private String code;
    private String uuid;
}
