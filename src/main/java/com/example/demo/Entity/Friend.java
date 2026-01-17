package com.example.demo.Entity;

import lombok.Data;

@Data
public class Friend {
    private Long id;
    private Long userId_1;
    private Long userId_2;
    private Long create_time;

    public Friend(Long userId_1,Long userId_2,Long create_time){
        this.userId_1 = userId_1;
        this.userId_2 = userId_2;
        this.create_time = create_time;
    }
}
