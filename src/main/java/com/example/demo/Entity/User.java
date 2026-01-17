package com.example.demo.Entity;

public class User {
    private Long id;
    private String account;
    private String emailAddr;
    private String name;
    private String password;
    private String avatar;
    private String signature;
    private Long createTime;

    public User(Long id,String account,String emailAddr,String name,String password,String avatar,Long createTime){
        this.id = id;
        this.account = account;
        this.emailAddr = emailAddr;
        this.name = name;
        this.password = password;
        this.avatar = avatar;
        this.createTime = createTime;
    }


    public String getAccount() {
        return account;
    }

    public String getEmailAddr() {
        return emailAddr;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public Long getId() {
        return id;
    }
}
