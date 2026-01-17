package com.example.demo.Provider;


import org.apache.ibatis.jdbc.SQL;
import java.util.Map;

public class StorageProvider {

    // 插入 storage
    public String insertStorage() {
        return new SQL(){{
            INSERT_INTO("storage");
            VALUES("id", "#{id}");
            VALUES("content", "#{content}");
            VALUES("type", "#{type}");
            VALUES("owner_id", "#{owner_id}");
            VALUES("send_time", "#{send_time}");
        }}.toString();
    }

    // 查询 storage（可按 type 过滤 + 分页）
    public String selectStorage(final Map<String,Object> params) {
        String type = (String) params.get("type");

        return new SQL(){{
            SELECT("id, content, type, owner_id, send_time");
            FROM("storage");
            WHERE("owner_id = #{userId}");

            if (type != null && !type.isEmpty()) {
                WHERE("type = #{type}");
            }

            ORDER_BY("send_time DESC");
        }} + " LIMIT #{limit} OFFSET #{offset}";
    }


}