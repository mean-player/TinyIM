package com.example.demo.VO;

import lombok.Data;

@Data
public class GroupShortInfo {
    private Long id;
    private String name;
    private String avatar;
    private String signature;
    private Integer member_count;

    public GroupShortInfo(Long id,String name,String avatar,String signature,Integer member_count){
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.signature = signature;
        this.member_count = member_count;
    }

}
