package com.example.demo.Mapper;



import com.example.demo.Provider.FileMetaProvider;
import com.example.demo.Entity.FileMeta;
import org.apache.ibatis.annotations.*;

@Mapper
public interface FileMetaMapper {

    @InsertProvider(type = FileMetaProvider.class, method = "insertFileMeta")
    int insertFileMeta(FileMeta fileMeta);


    @UpdateProvider(type = FileMetaProvider.class, method = "updateIsCompleted")
    int updateIsCompleted(
            @Param("userId") String userId,
            @Param("filehash") String filehash,
            @Param("is_completed") boolean is_completed
    );


    @SelectProvider(type = FileMetaProvider.class, method = "selectFileMeta")
    FileMeta selectFileMeta(
            @Param("userId") String userId,
            @Param("filehash") String filehash
    );
}