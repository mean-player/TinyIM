package com.example.demo.Repository;

import com.example.demo.Entity.Storage;
import com.example.demo.Mapper.StorageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StorageRepository {

    private final StorageMapper storageMapper;

    public void insertStorage(Storage storage){
        storageMapper.insertStorage(storage);
    }

    public List<Storage> selectStorage(Long userId,String type,Integer limit,Integer offset){
        return storageMapper.selectStorage(userId,type,limit,offset);
    }


}
