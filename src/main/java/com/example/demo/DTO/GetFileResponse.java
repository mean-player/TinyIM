package com.example.demo.DTO;

public class GetFileResponse {
    private String url;
    private String filename;
    private String type;
    private Long size;

    public GetFileResponse(String url, String filename, String type, Long size){
        this.url = url;
        this.filename = filename;
        this.type = type;
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public String getFilename() {
        return filename;
    }

    public String getType() {
        return type;
    }

    public Long getSize() {
        return size;
    }
}
