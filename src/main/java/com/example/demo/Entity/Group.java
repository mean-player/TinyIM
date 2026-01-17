package com.example.demo.Entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Group {
    private Long id;
    private String name;
    private Long owner_id;
    private String avatar;
    private String signature;
    private Integer member_count;
    private Long create_time;

    public Group(Long id,String name,Long owner_id,String avatar,String signature,Integer member_count,Long create_time){
        this.id = id;
        this.name = name;
        this.owner_id = owner_id;
        this.avatar = avatar;
        this.signature = signature;
        this.member_count = member_count;
        this.create_time = create_time;
    }

}
