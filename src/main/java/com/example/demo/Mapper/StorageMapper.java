package com.example.demo.Mapper;


import com.example.demo.Entity.Storage;
import com.example.demo.Provider.StorageProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import java.util.List;

@Mapper
public interface StorageMapper {

    @InsertProvider(type = StorageProvider.class, method = "insertStorage")
    void insertStorage(Storage storage);

    @SelectProvider(type = StorageProvider.class, method = "selectStorage")
    List<Storage> selectStorage(
            @Param("userId") Long userId,
            @Param("type") String type,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );


}