package com.example.demo.DTO;

import java.time.LocalDateTime;

public class WSRequest {
    private String sender;
    private Long receiver;
    private String content;
    private LocalDateTime send_time;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Long getReceiver() {
        return receiver;
    }

    public void setReceiver(Long receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSend_time() {
        return send_time;
    }

    public void setSend_time(LocalDateTime send_time) {
        this.send_time = send_time;
    }
}
