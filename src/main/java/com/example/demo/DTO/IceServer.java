package com.example.demo.DTO;

import java.util.List;

public class IceServer {
    private List<String> urls;
    private String username;
    private String credential;

    public String getUsername() {
        return username;
    }



    public List<String> getUrls() {
        return urls;
    }

    public IceServer(List<String> urls, String username, String credential) {
        this.urls=urls;
        this.username = username;
        this.credential = credential;
    }

    public String getCredential() {
        return credential;
    }
}
