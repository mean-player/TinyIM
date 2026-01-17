package com.example.demo.DTO;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ApplicationReq {
    private String from;
    private String to;
    private String type;
    @Size(max = 50,message = "申请理由不能超过50个字符")
    private String app_desc;
}
