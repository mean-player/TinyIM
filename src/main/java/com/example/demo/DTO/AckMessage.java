package com.example.demo.DTO;

import lombok.Data;

@Data
public class AckMessage {
    private String uuid;
    private Boolean sent;

    public AckMessage(String uuid,Boolean sent){
        this.uuid = uuid;
        this.sent = sent;
    }
}
