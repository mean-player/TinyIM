package com.example.demo.Repository;


import com.example.demo.Entity.Message;
import com.example.demo.Mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MessageRepository {
    private final MessageMapper messageMapper;

    public Long selectMaxSeq(String conversationId){
        return messageMapper.selectMaxSeq(conversationId);
    }

    public Message selectById(Long messageId){
        return messageMapper.selectById(messageId);
    }

    public Message selectBySeqCon(Long seq,String conversationId){
        return messageMapper.selectBySeqCon(seq,conversationId);
    }

    public void insertMessage(Message message){
        messageMapper.insertMessage(message);
    };

    public void insertBatch(List<Message>messages){messageMapper.insertBatch(messages);}



}
