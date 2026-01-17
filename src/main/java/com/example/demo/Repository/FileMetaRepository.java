package com.example.demo.Repository;


import com.example.demo.Entity.FileMeta;
import com.example.demo.Mapper.FileMetaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FileMetaRepository {
    private final FileMetaMapper fileMetaMapper;

    public void insertFileMeta(FileMeta fileMeta){
        fileMetaMapper.insertFileMeta(fileMeta);
    }

    public void updateIsCompleted(Long userId,String filehash,Boolean is_completed){
        fileMetaMapper.updateIsCompleted(String.valueOf(userId),filehash,is_completed);
    }

    public FileMeta selectFileMeta(Long userId,String filehash){
        return fileMetaMapper.selectFileMeta(String.valueOf(userId),filehash);
    }
}
