package com.example.demo.Service;

import com.example.demo.Component.MinioProperties;
import com.example.demo.Component.MinioWorker;
import com.example.demo.DTO.UploadInitResponse;
import com.example.demo.DTO.GetFileResponse;
import com.example.demo.Entity.FileMeta;
import com.example.demo.Repository.FileMetaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor

public class UploadService {

    private final MinioWorker minioWorker;

    private final MinioProperties minioProperties;

    private final FileMetaRepository fileMetaRepository;

    private String generatorUploadIdKey(Long userId,String filehash){
        return "uploadId:"+userId+":"+filehash;
    }
    private String generatorKey(Long userId,String filehash){
        return userId+"/"+filehash;
    }


    public boolean easyUpload(Long userId,String filehash,String filename,Long size,String type,MultipartFile file,boolean isPrivate){
        String bucket = minioProperties.getBucket();
        String key = generatorKey(userId,filehash);
        FileMeta fileMeta = new FileMeta(filehash,filename,type,userId,size,isPrivate,bucket,true);
        fileMetaRepository.insertFileMeta(fileMeta);

        return minioWorker.easyUpload(bucket,key,file);
    }



    public UploadInitResponse initUpload(Long userId,String filehash,String filename,Long size,
                                         String type,boolean isPrivate,Integer totalParts){
        String bucket = minioProperties.getBucket();
        String key = generatorKey(userId,filehash);
        String uploadIdKey = generatorUploadIdKey(userId,filehash);
        UploadInitResponse uploadInitResponse = minioWorker.initUpload(bucket,key,totalParts,uploadIdKey);
        if(uploadInitResponse.getNewCreated()) {
            FileMeta fileMeta = new FileMeta(filehash, filename, type, userId, size, isPrivate, bucket, false);
            fileMetaRepository.insertFileMeta(fileMeta);
        }

        return uploadInitResponse;
    }

    public boolean uploadPart(Long userId, String filehash, Integer partNumber, MultipartFile chunkFile){
        String uploadIdKey = generatorUploadIdKey(userId,filehash);
        String uploadId = minioWorker.getUploadIdByKey(uploadIdKey);
        log.info("uploadPart userId={} filehash={} partNumber={} uploadId={}", userId, filehash, partNumber, uploadId);
        try(InputStream in = chunkFile.getInputStream()){
            minioWorker.uploadPart(uploadId,partNumber,in,chunkFile.getSize());
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }


    public List<Integer> getMissingParts(Long userId, String filehash){
        String uploadIdKey = generatorUploadIdKey(userId,filehash);
        String uploadId = minioWorker.getUploadIdByKey(uploadIdKey);
        log.info("getMissingParts userId={} filehash={} uploadId={}", userId, filehash, uploadId);
        return minioWorker.getMissingParts(uploadId);
    }

    public boolean completeUpload(Long userId, String filehash){
        String uploadIdKey = generatorUploadIdKey(userId,filehash);
        String uploadId = minioWorker.getUploadIdByKey(uploadIdKey);
        log.info("completeUpload userId={} filehash={} uploadId={}", userId, filehash, uploadId);

        if(minioWorker.completeUpload(uploadId)){
            fileMetaRepository.updateIsCompleted(userId,filehash,true);
            return true;
        }else{
            return false;
        }
    }


    public GetFileResponse getUrl(Long userId, String filehash){
        FileMeta fileMeta = fileMetaRepository.selectFileMeta(userId,filehash);
        String bucket = fileMeta.getBucket();
        String key = generatorKey(userId,filehash);
        String url =  minioWorker.getUrl(bucket,key,minioProperties.getEndpoint(),minioProperties.getAccessKey(),minioProperties.getSecretKey());
        GetFileResponse getFileResponse = new GetFileResponse(url,fileMeta.getFilename(),fileMeta.getType(),fileMeta.getSize());
        return getFileResponse;

    }

}
