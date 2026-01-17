package com.example.demo.DTO;

import lombok.Data;

@Data
public class CallResponse {
    private String type;//approve   reject
    private String from;
    private String to;
}
