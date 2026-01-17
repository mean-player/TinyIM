package com.example.demo.DTO;

import lombok.Data;

@Data
public class CandidateMessage {
    private String from;
    private String to;
    private Candidate candidate;

}
