package com.example.demo.Entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Application {
    private Long id;
    private Long from_id;
    private Long to_id;
    private Integer status;// -1---不通过   0---申请中  1---同意
    private Boolean is_read;//true --已读
    private String type;// FRIEND  GROUP
    private Long create_time;
    private String app_desc;

    public Application(Long id,Long from,Long to,Integer status,Boolean read,String type,Long create_time,String app_desc){
        this.id = id;
        this.from_id = from;
        this.to_id =to;
        this.status = status;
        this.is_read = read;
        this.type = type;
        this.create_time = create_time;
        this.app_desc = app_desc;
    }
}
