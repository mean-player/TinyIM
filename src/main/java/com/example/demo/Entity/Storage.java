package com.example.demo.Entity;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Storage {
    private Long id;
    private String content; //txt or url
    private String type;// txt,image,audio,video,others
    private Long owner_id;
    private Long send_time;

    public Storage(Long id,String content,String type,Long owner_id,Long send_time){
        this.id = id;
        this.content = content;
        this.type = type;
        this.owner_id = owner_id;
        this.send_time = send_time;
    }
}
