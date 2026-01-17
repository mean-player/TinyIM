package com.example.demo.DTO;

import lombok.Data;

@Data
public class StorageReq {
    private String uuid;
    private String content;
    private MessageType type;

    public String getType(){
        return type != null ? type.name() : null;
    }
}
