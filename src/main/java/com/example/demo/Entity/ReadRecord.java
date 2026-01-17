package com.example.demo.Entity;

import lombok.Data;

@Data
public class ReadRecord {
    private Long id;
    private Long user_id;
    private String conversation_id;
    private Long seq;
}
