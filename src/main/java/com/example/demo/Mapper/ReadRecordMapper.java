package com.example.demo.Mapper;


import com.example.demo.Entity.ReadRecord;
import com.example.demo.Provider.ReadRecordProvider;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReadRecordMapper {

    @InsertProvider(type = ReadRecordProvider.class, method = "insertReadRecord")
    void insertReadRecord(
            @Param("userId") Long userId,
            @Param("conversationId") String conversationId,
            @Param("seq") Long seq
    );

    @UpdateProvider(type = ReadRecordProvider.class, method = "updateSeq")
    void updateSeq(
            @Param("userId") Long userId,
            @Param("conversationId") String conversationId,
            @Param("newSeq") Long newSeq
    );

    @SelectProvider(type = ReadRecordProvider.class, method = "selectReadRecords")
    List<ReadRecord> selectReadRecords(@Param("userId") Long userId);

    @DeleteProvider(type = ReadRecordProvider.class, method = "removeReadRecord")
    void removeReadRecord(
            @Param("userId") Long userId,
            @Param("conversationId") String conversationId
    );
}