package com.example.demo.DTO;

public class Candidate {
    private String candidate; // candidate 字符串
    private String sdpMid;    // 通道标识
    private Integer sdpMLineIndex; // 通道索引

    public Candidate() {}
    public Candidate(String candidate, String sdpMid, Integer sdpMLineIndex) {
        this.candidate = candidate;
        this.sdpMid = sdpMid;
        this.sdpMLineIndex = sdpMLineIndex;
    }

    // getter & setter
    public String getCandidate() { return candidate; }
    public void setCandidate(String candidate) { this.candidate = candidate; }

    public String getSdpMid() { return sdpMid; }
    public void setSdpMid(String sdpMid) { this.sdpMid = sdpMid; }

    public Integer getSdpMLineIndex() { return sdpMLineIndex; }
    public void setSdpMLineIndex(Integer sdpMLineIndex) { this.sdpMLineIndex = sdpMLineIndex; }

}