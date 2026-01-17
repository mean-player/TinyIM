package com.example.demo.Provider;

import org.apache.ibatis.jdbc.SQL;

public class FriendProvider {

    public String selectFriendIds() {
        return new SQL(){{
            SELECT("CASE WHEN user_id_1 = #{userId} THEN user_id_2 ELSE user_id_1 END AS friendId");
            FROM("friend");
            WHERE("user_id_1 = #{userId} OR user_id_2 = #{userId}");
        }}.toString();
    }

    public String addFriend(){
        return new SQL(){
            {
                INSERT_INTO("friend");
                VALUES("user_id_1","#{userId_1}");
                VALUES("user_id_2","#{userId_2}");
                VALUES("create_time","#{create_time}");

            }
        }.toString();

    }

    public String removeFriend(){
        return new SQL(){
            {
                DELETE_FROM("friend");
                WHERE("user_id_1=#{userId_1} AND user_id_2=#{userId_2}"+
                        "OR user_id_1=#{userId_2} AND user_id_2=#{userId_1}");
            }
        }.toString();
    }
}
