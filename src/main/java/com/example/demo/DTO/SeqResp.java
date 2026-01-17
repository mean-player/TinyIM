package com.example.demo.DTO;

import lombok.Data;

@Data
public class SeqResp {
    private Long seq;
    private String conversation_id;
    private String relation_type;

    public SeqResp(Long seq,String conversation_id,String relation_type){
        this.seq = seq;
        this.conversation_id = conversation_id;
        this.relation_type = relation_type;

    }
}
