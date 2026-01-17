package com.example.demo.Provider;



import com.example.demo.Entity.Message;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.jdbc.SQL;

import java.util.List;

public class MessageProvider {

    // 查询某个会话的最大 seq
    public String selectMaxSeq() {
        return new SQL(){{
            SELECT("MAX(seq)");
            FROM("message");
            WHERE("conversation_id = #{conversationId}");
        }}.toString();
    }

    // 根据 messageId 查询
    public String selectById() {
        return new SQL(){{
            SELECT("id, seq, conversation_id, content, type, sender_id, receiver_id, send_time");
            FROM("message");
            WHERE("id = #{messageId}");
        }}.toString();
    }

    // 根据 seq 和 conversationId 查询消息（用于补齐丢失消息）
    public String selectBySeqCon() {
        return new SQL(){{
            SELECT("id, seq, conversation_id, content, type, sender_id, receiver_id, send_time");
            FROM("message");
            WHERE("seq = #{seq}");
            WHERE("conversation_id = #{conversationId}");
        }}.toString();
    }

    // 插入消息
    public String insertMessage() {
        return new SQL(){{
            INSERT_INTO("message");
            VALUES("id", "#{id}");
            VALUES("seq", "#{seq}");
            VALUES("conversation_id", "#{conversation_id}");
            VALUES("content", "#{content}");
            VALUES("type", "#{type}");
            VALUES("sender_id", "#{sender_id}");
            VALUES("receiver_id", "#{receiver_id}");
            VALUES("send_time", "#{send_time}");
        }}.toString();
    }


    //批量插入
    public String insertBatch(@Param("list") List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return ""; // 避免报错
        }

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO message (id, seq, conversation_id, content, type, sender_id, receiver_id, send_time) VALUES ");

        for (int i = 0; i < messages.size(); i++) {
            sb.append("(")
                    .append("#{list[").append(i).append("].id},")
                    .append("#{list[").append(i).append("].seq},")
                    .append("#{list[").append(i).append("].conversation_id},")
                    .append("#{list[").append(i).append("].content},")
                    .append("#{list[").append(i).append("].type},")
                    .append("#{list[").append(i).append("].sender_id},")
                    .append("#{list[").append(i).append("].receiver_id},")
                    .append("#{list[").append(i).append("].send_time}")
                    .append(")");
            if (i < messages.size() - 1) {
                sb.append(",");
            }
        }

        return sb.toString();
    }
}