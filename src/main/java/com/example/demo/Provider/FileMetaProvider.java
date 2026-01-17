package com.example.demo.Provider;

import org.apache.ibatis.jdbc.SQL;

public class FileMetaProvider {

    public String insertFileMeta(){
        return new SQL(){
            {
                INSERT_INTO("file_meta");
                VALUES("id","#{id}");
                VALUES("filehash","#{filehash}");
                VALUES("filename","#{filename}");
                VALUES("type","#{type}");
                VALUES("owner_id","#{owner_id}");
                VALUES("size","#{size}");
                VALUES("is_private","#{isPrivate}");
                VALUES("bucket","#{bucket}");
                VALUES("is_completed","#{isCompleted}");
            }
        }.toString();
    }

    public String updateIsCompleted(){
        return new SQL(){
            {
                UPDATE("file_meta");
                SET("is_completed=#{is_completed}");
                WHERE("owner_id=#{userId}");
                WHERE("filehash=#{filehash}");

            }
        }.toString();
    }

    public String selectFileMeta(){
        return new SQL(){
            {
                SELECT("*");
                FROM("file_meta");
                WHERE("owner_id=#{userId}");
                WHERE("filehash=#{filehash}");

            }
        }.toString();
    }
}
