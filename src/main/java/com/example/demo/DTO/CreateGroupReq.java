package com.example.demo.DTO;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateGroupReq {
    @Size(max = 10,message = "群名称不能超过10个字符")
    private String name;
    @Size(max = 100,message = "群描述不能超过100个字符")
    private String signature;
}
