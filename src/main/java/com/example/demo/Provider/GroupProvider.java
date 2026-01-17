package com.example.demo.Provider;



import org.apache.ibatis.jdbc.SQL;
import java.util.Map;

public class GroupProvider {

    // 创建群组
    public String createGroup() {
        return new SQL(){{
            INSERT_INTO("group_info");
            VALUES("id", "#{id}");
            VALUES("name", "#{name}");
            VALUES("owner_id", "#{owner_id}");
            VALUES("avatar", "#{avatar}");
            VALUES("signature", "#{signature}");
            VALUES("member_count", "#{member_count}");
            VALUES("create_time", "#{create_time}");
        }}.toString();
    }

    // 解散群
    public String disbandGroup() {
        return new SQL(){{
            DELETE_FROM("group_info");
            WHERE("id = #{groupId}");
        }}.toString();
    }

    // 查询 owner_id
    public String findOwner() {
        return new SQL(){{
            SELECT("owner_id");
            FROM("group_info");
            WHERE("id = #{groupId}");
        }}.toString();
    }

    // 更新名字
    public String updateName() {
        return new SQL(){{
            UPDATE("group_info");
            SET("name = #{newName}");
            WHERE("id = #{groupId}");
        }}.toString();
    }

    // 更新签名
    public String updateSignature() {
        return new SQL(){{
            UPDATE("group_info");
            SET("signature = #{newSignature}");
            WHERE("id = #{groupId}");
        }}.toString();
    }

    // 更新头像
    public String updateAvatar() {
        return new SQL(){{
            UPDATE("group_info");
            SET("avatar = #{newAvatar}");
            WHERE("id = #{groupId}");
        }}.toString();
    }

    // 成员数 +1
    public String increaseCount() {
        return new SQL(){{
            UPDATE("group_info");
            SET("member_count = member_count + 1");
            WHERE("id = #{groupId}");
        }}.toString();
    }

    // 成员数 -1
    public String decreaseCount() {
        return new SQL(){{
            UPDATE("group_info");
            SET("member_count = member_count - 1");
            WHERE("id = #{groupId}");
        }}.toString();
    }

    // 根据 id 查询 GroupShortInfo
    public String selectVOById() {
        return new SQL(){{
            SELECT("*");
            FROM("view_group_info");
            WHERE("id = #{groupId}");
        }}.toString();
    }


}