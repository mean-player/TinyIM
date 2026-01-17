package com.example.demo.Mapper;


import com.example.demo.Entity.Message;
import com.example.demo.Provider.MessageProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

@Mapper
public interface MessageMapper {

    @SelectProvider(type = MessageProvider.class, method = "selectMaxSeq")
    Long selectMaxSeq(@Param("conversationId") String conversationId);

    @SelectProvider(type = MessageProvider.class, method = "selectById")
    Message selectById(@Param("messageId") Long messageId);

    @SelectProvider(type = MessageProvider.class, method = "selectBySeqCon")
    Message selectBySeqCon(@Param("seq") Long seq, @Param("conversationId") String conversationId);

    @InsertProvider(type = MessageProvider.class, method = "insertMessage")
    void insertMessage(Message msg);

    @InsertProvider(type = MessageProvider.class, method = "insertBatch")
    void insertBatch(@Param("list") List<Message> messages);
}