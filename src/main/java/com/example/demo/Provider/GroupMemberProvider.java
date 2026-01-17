package com.example.demo.Provider;

import org.apache.ibatis.jdbc.SQL;

public class GroupMemberProvider {

    public String selectGroups(){
        return new SQL(){
            {
                SELECT("group_id");
                FROM("group_member");
                WHERE("user_id=#{user_id}");
            }
        }.toString();
    }

    public String selectMembers(){
        return new SQL(){
            {
                SELECT("user_id");
                FROM("group_member");
                WHERE("group_id=#{group_id}");
            }
        }.toString();
    }

    public String addGroupMember(){
        return new SQL(){
            {
                INSERT_INTO("group_member");
                VALUES("group_id","#{group_id}");
                VALUES("user_id","#{user_id}");
                VALUES("join_time","#{join_time}");
                VALUES("role","#{role}");
            }
        }.toString();
    }

    public String removeMember(){
        return new SQL(){
            {
                DELETE_FROM("group_member");
                WHERE("user_id=#{user_id} AND group_id=#{group_id}");
            }
        }.toString();
    }

    public String disbandGroup(){
        return new SQL(){
            {
                DELETE_FROM("group_member");
                WHERE("group_id=#{group_id}");
            }
        }.toString();
    }



}
