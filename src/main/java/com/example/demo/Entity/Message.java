package com.example.demo.Entity;

import com.example.demo.DTO.MessageType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Message {
    private Long id;
    private Long seq;
    private String conversation_id;
    //min(userId_1,userId_2)+ _ + max(userId_1,userId_2)  or g_ + group_id
    private String content; //txt or file_url
    private String type;
    private Long sender_id;
    private Long receiver_id;
    private Long send_time;



    public Message(Long id,Long seq,String conversation_id,String content,String type,Long sender_id,Long receiver_id,Long send_time){

        this.id = id;
        this.seq = seq;
        this.conversation_id = conversation_id;
        this.content = content;
        this.type = type;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
        this.send_time = send_time;
    }



}
