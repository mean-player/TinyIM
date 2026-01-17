package com.example.demo.DTO;

import lombok.Data;

@Data
public class MessageSendReq {

    private String sender_id;
    private String receiver_id;
    private String content;
    private MessageType type;
    private RelationType relationType;

    public String getType(){
        return type != null ? type.name() : null;
    }

    public String getRelationType(){
        return relationType !=null ? relationType.name() : null;
    }
}
