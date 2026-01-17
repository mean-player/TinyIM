package com.example.demo.Provider;



import org.apache.ibatis.jdbc.SQL;

public class ReadRecordProvider {

    // 插入已读记录（新增好友时 seq = -1）
    public String insertReadRecord() {
        return new SQL(){{
            INSERT_INTO("read_record");
            VALUES("user_id", "#{userId}");
            VALUES("conversation_id", "#{conversationId}");
            VALUES("seq", "#{seq}");
        }}.toString();
    }

    // 更新 seq（用户滑动到最新消息时更新）
    public String updateSeq() {
        return new SQL(){{
            UPDATE("read_record");
            SET("seq = #{newSeq}");
            WHERE("user_id = #{userId}");
            WHERE("conversation_id = #{conversationId}");
        }}.toString();
    }

    // 查询用户所有的已读记录
    public String selectReadRecords() {
        return new SQL(){{
            SELECT("id, user_id, conversation_id, seq");
            FROM("read_record");
            WHERE("user_id = #{userId}");
        }}.toString();
    }

    // 删除某条会话的已读记录（比如从对话列表移除）
    public String removeReadRecord() {
        return new SQL(){{
            DELETE_FROM("read_record");
            WHERE("user_id = #{userId}");
            WHERE("conversation_id = #{conversationId}");
        }}.toString();
    }
}