package com.example.demo.DTO;

import lombok.Data;

@Data
public class AckReadInfo {
    String conversationId;
    Long newSeq;
}
