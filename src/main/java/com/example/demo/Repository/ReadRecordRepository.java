package com.example.demo.Repository;

import com.example.demo.Entity.ReadRecord;
import com.example.demo.Mapper.ReadRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReadRecordRepository {

    private final ReadRecordMapper readRecordMapper;

    public void insertReadRecord(Long userId,String conversationId,Long seq){
        readRecordMapper.insertReadRecord(userId,conversationId,seq);
    }//新增好友时创建 seq为-1

    public void updateSeq(Long userId,String conversationId,Long newSeq){
        readRecordMapper.updateSeq(userId,conversationId,newSeq);
    }

    public List<ReadRecord>selectReadRecords(Long userId){
        return readRecordMapper.selectReadRecords(userId);
    }

    public void removeReadRecord(Long userId,String conversationId){
        readRecordMapper.removeReadRecord(userId,conversationId);
    }

}
