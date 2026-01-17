package com.example.demo.Provider;

import org.apache.ibatis.jdbc.SQL;

public class ApplicationProvider {

    public String addApplication(){
        return new SQL(){
            {
                INSERT_INTO("application");
                VALUES("id","#{id}");
                VALUES("from_id","#{from_id}");
                VALUES("to_id","#{to_id}");
                VALUES("status","#{status}");
                VALUES("is_read","#{is_read}");
                VALUES("type","#{type}");
                VALUES("create_time","#{create_time}");
                VALUES("app_desc","#{app_desc}");
            }
        }.toString();
    }

    public String selectById(){
        return new SQL(){
            {
                SELECT("*");
                FROM("application");
                WHERE("id = #{id}");
            }
        }.toString();
    }

    public String selectByFT(){
        return new SQL(){
            {
                SELECT("*");
                FROM("application");
                WHERE("from_id=#{from_id}");
                WHERE("to_id=#{to_id}");
            }
        }.toString();
    }

    public String updateStatus(){
        return new SQL(){
            {
                UPDATE("application");
                SET("status=#{status}");
                WHERE("id=#{id}");
            }
        }.toString();
    }

    public String updateRead(){
        return new SQL(){
            {
                UPDATE("application");
                SET("is_read=#{is_read}");
                WHERE("id=#{id}");
            }
        }.toString();
    }

    public String selectSentApplication(){
        return new SQL(){
            {
                SELECT("*");
                FROM("application");
                WHERE("from_id=#{from_id}");
            }
        }.toString();
    }

    public String selectReceiveApplication(){
        return new SQL(){
            {
                SELECT("*");
                FROM("application");
                WHERE("to_id=#{to_id}");
            }
        }.toString();
    }


}
