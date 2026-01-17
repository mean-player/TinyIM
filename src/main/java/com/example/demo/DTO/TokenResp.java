package com.example.demo.DTO;


import lombok.Data;

@Data
public class TokenResp {
    private String accessToken;
    private String refreshToken;
}
