package com.example.demo.Entity;

public class FileMeta {
    private Long id;
    private String filehash;
    private String filename;
    private String type;
    private Long owner_id;
    private Long size;
    private Boolean isPrivate;
    private String bucket;
    private Boolean isCompleted;

    public FileMeta(String filehash, String filename, String type, Long owner_id, Long size,
                    Boolean isPrivate,String bucket,Boolean isCompleted){
        this.filehash = filehash;
        this.filename  = filename;
        this.type = type;
        this.owner_id = owner_id;
        this.size = size;
        this.isPrivate = isPrivate;
        this.bucket = bucket;
        this.isCompleted = isCompleted;
    }
    public String getFilename() {
        return filename;
    }

    public Long getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    public Long getOwner_id() {
        return owner_id;
    }

    public Boolean getIsPrivate() {
        return isPrivate;
    }

    public String getFilehash() {
        return filehash;
    }

    public String getBucket() {
        return bucket;
    }

    public Boolean getIsCompleted() {
        return isCompleted;
    }



}
