package com.example.demo.DTO;

import lombok.Data;

@Data
public class SignalMessage {
    private SdpMessage sdp;  // SDP 字符串
    private String from; // 发送方标识
    private String to;   // 接收方标识

    // 构造器
    public SignalMessage() {}
    public SignalMessage(SdpMessage sdp, String from, String to) {
        this.sdp = sdp;
        this.from = from;
        this.to = to;
    }


}