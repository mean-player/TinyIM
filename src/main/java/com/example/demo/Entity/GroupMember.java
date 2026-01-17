package com.example.demo.Entity;

import lombok.Data;
import org.springframework.security.core.parameters.P;

@Data
public class GroupMember {
    private Long id;
    private Long group_id;
    private Long user_id;
    private Long join_time;
    private String role;

    public GroupMember(Long group_id,Long user_id,Long join_time,String role){
        this.group_id = group_id;
        this.user_id = user_id;
        this.join_time = join_time;
        this.role = role;
    }

}
