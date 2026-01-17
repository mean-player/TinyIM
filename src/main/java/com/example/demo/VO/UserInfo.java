package com.example.demo.VO;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class UserInfo {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String account;
    private String name;
    private String avatar;
    private String signature;

    public UserInfo(Long id,String account,String name,String avatar,String signature){
        this.id = id;
        this.account = account;
        this.name = name;
        this.avatar = avatar;
        this.signature = signature;
    }
}