package com.example.demo.Provider;


import org.apache.ibatis.jdbc.SQL;
import java.util.List;
import java.util.Map;

public class UserProvider {

    public String selectById(Map<String, Object> params) {
        return new SQL(){{
            SELECT("*");
            FROM("user");
            WHERE("id = #{id}");
        }}.toString();
    }

    public String insertUser(Map<String, Object> params) {
        return new SQL(){{
            INSERT_INTO("user");
            VALUES("id", "#{user.id}");
            VALUES("account", "#{user.account}");
            VALUES("email_addr", "#{user.emailAddr}");
            VALUES("name", "#{user.name}");
            VALUES("password", "#{user.password}");
            VALUES("avatar", "#{user.avatar}");
            VALUES("signature", "#{user.signature}");
            VALUES("create_time", "#{user.createTime}");
        }}.toString();
    }

    public String selectIdByAccount(Map<String, Object> params) {
        return new SQL(){{
            SELECT("id");
            FROM("user");
            WHERE("account = #{account}");
        }}.toString();
    }

    public String existByEmail(Map<String, Object> params) {
        return new SQL(){{
            SELECT("count(*)");
            FROM("user");
            WHERE("email_addr = #{emailAddr}");
        }}.toString();
    }

    public String selectByAccount(Map<String, Object> params) {
        return new SQL(){{
            SELECT("*");
            FROM("user");
            WHERE("account = #{account}");
        }}.toString();
    }

    public String selectByEmailAddr(Map<String, Object> params) {
        return new SQL(){{
            SELECT("*");
            FROM("user");
            WHERE("email_addr = #{emailAddr}");
        }}.toString();
    }

    // ---- View User Info ----
    public String selectInfoById(Map<String, Object> params) {
        return new SQL(){{
            SELECT("*");
            FROM("view_user_info");
            WHERE("id = #{id}");
        }}.toString();
    }

    public String selectByIdList(Map<String, Object> params) {
        List<Long> ids = (List<Long>) params.get("ids");

        StringBuilder sb = new StringBuilder();
        sb.append("select id,account,name,avatar,signature from view_user_info where id in (");

        for (int i = 0; i < ids.size(); i++) {
            sb.append("#{ids[").append(i).append("]}");
            if (i < ids.size() - 1) sb.append(",");
        }
        sb.append(")");

        return sb.toString();
    }

    public String updateAvatar(Map<String, Object> params) {
        return new SQL(){{
            UPDATE("user");
            SET("avatar = #{avatar}");
            WHERE("id = #{userId}");
        }}.toString();
    }

    public String updateSignature(Map<String, Object> params) {
        return new SQL(){{
            UPDATE("user");
            SET("signature = #{signature}");
            WHERE("id = #{userId}");
        }}.toString();
    }

    public String updateName(Map<String, Object> params) {
        return new SQL(){{
            UPDATE("user");
            SET("name = #{name}");
            WHERE("id = #{userId}");
        }}.toString();
    }

    public String updatePassword(Map<String, Object> params) {
        return new SQL(){{
            UPDATE("user");
            SET("password = #{password}");
            WHERE("id = #{userId}");
        }}.toString();
    }

    public String selectEmailById(Map<String, Object> params) {
        return new SQL(){{
            SELECT("email_addr");
            FROM("user");
            WHERE("id = #{id}");
        }}.toString();
    }
}
