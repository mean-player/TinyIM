package com.example.demo.Component;


import com.example.demo.DTO.UploadInitResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * MinioWorker - 使用 AWS S3 SDK (v2) 操作 MinIO/S3。
 *
 *
 * 分块数量： 1~totalParts
 *
 * Redis 数据：
 *  - upload:{uploadId}:meta  (hash) -> bucket, key, totalParts (可选)
 *  - upload:{uploadId}:bitmap (bitmap) -> 分块是否上传成功（bit 位从 1 开始）
 *  - upload:{uploadId}:etags (hash) -> field=partNumber, value=eTag
 */
@Component
@Slf4j
public class MinioWorker {

    private final S3Client s3Client;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration redisTtl = Duration.ofDays(1); // TTL，用于过期回收

    public MinioWorker(S3Client s3Client, RedisTemplate<String, Object> redisTemplate) {
        this.s3Client = s3Client;
        this.redisTemplate = redisTemplate;
    }

    private String keyPrefix(String uploadId) {
        return "upload:" + uploadId;
    }


    //一次性上传
    public boolean easyUpload(String bucket, String key, MultipartFile file){
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        try {
            s3Client.putObject(
                    putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );
            return true;
        }catch (IOException e){
            return false;
        }

    }



    //初始化 Multipart Upload
    //uploadIdKey : uploadId:{userId}:{filehash}
    public UploadInitResponse initUpload(String bucket, String key, Integer totalParts, String uploadIdKey) {
        UploadInitResponse uploadInitResponse = new UploadInitResponse();
        String existingUploadId = (String)redisTemplate.opsForValue().get(uploadIdKey);
        if(existingUploadId != null){
            uploadInitResponse.setUploadId(existingUploadId);
            uploadInitResponse.setNewCreated(false);
            log.info("initUpload reuse uploadId={}", existingUploadId);
            return uploadInitResponse;
        }
        CreateMultipartUploadResponse resp = s3Client.createMultipartUpload(
                CreateMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .key(key) //account/uuid
                        .build()
        );
        String uploadId = resp.uploadId();

        String prefix = keyPrefix(uploadId);
        Map<String, String> meta = new HashMap<>();
        meta.put("bucket", bucket);
        meta.put("key", key);
        meta.put("uploadId", uploadId);
        if (totalParts != null) meta.put("totalParts", String.valueOf(totalParts));
        // 保存 meta , uploadId 到 redis
        redisTemplate.opsForHash().putAll(prefix + ":meta", meta);
        redisTemplate.expire(prefix + ":meta", redisTtl);
        redisTemplate.opsForValue().set(uploadIdKey,uploadId);
        redisTemplate.expire(uploadIdKey,redisTtl);

        uploadInitResponse.setUploadId(uploadId);
        uploadInitResponse.setNewCreated(true);
        log.info("initUpload new uploadId={} bucket={} key={}", uploadId, bucket, key);
        return uploadInitResponse;
    }

    //上传单个分块
    @Async("taskExecutor")
    public void uploadPart(String uploadId, Integer partNumber, InputStream inputStream,long size){
        String prefix = keyPrefix(uploadId);
        String bitmapKey = prefix + ":bitmap";
        String etagsKey = prefix + ":etags";
        // 已上传跳过（由 bitmap 控制）
        Boolean exists = redisTemplate.opsForValue().getBit(bitmapKey, partNumber);
        if (Boolean.TRUE.equals(exists)) {
            // 已上传，直接返回 null（调用方需过滤 null）
            return ;
        }
        // 读取 meta
        String bucket = (String) redisTemplate.opsForHash().get(prefix + ":meta", "bucket");
        String key = (String) redisTemplate.opsForHash().get(prefix + ":meta", "key");
        if (bucket == null || key == null) throw new IllegalStateException("upload meta missing for " + uploadId);
        try{
            UploadPartRequest uploadReq = UploadPartRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .uploadId(uploadId)
                    .partNumber(partNumber)
                    .contentLength(size)
                    .build();

            UploadPartResponse uploadResp = s3Client.uploadPart(uploadReq, RequestBody.fromInputStream(inputStream,size));
            String etag = uploadResp.eTag();
            // 存 Redis: bitmap + etag
            redisTemplate.opsForValue().setBit(bitmapKey, partNumber, true);
            redisTemplate.opsForHash().put(etagsKey, String.valueOf(partNumber), etag);
            redisTemplate.expire(bitmapKey, redisTtl);
            redisTemplate.expire(etagsKey, redisTtl);
            log.info("uploadPart uploadId={} partNumber={} etag={}", uploadId, partNumber, etag);

        }catch (Exception ex) {
            throw new CompletionException(ex);
        }
    }


    //合并分块（从 Redis 读取 etags）,说明：parts 必须按 partNumber 升序
    public boolean completeUpload(String uploadId) {
        String prefix = keyPrefix(uploadId);
        Map<Object, Object> etags = redisTemplate.opsForHash().entries(prefix + ":etags");
        if (etags == null || etags.isEmpty()) {
            throw new IllegalStateException("No parts found in redis for uploadId=" + uploadId);
        }

        List<CompletedPart> parts = etags.entrySet().stream()
                .map(e -> CompletedPart.builder()
                        .partNumber(Integer.parseInt((String) e.getKey()))
                        .eTag((String) e.getValue())
                        .build())
                .sorted(Comparator.comparingInt(CompletedPart::partNumber))
                .collect(Collectors.toList());

        String bucket = (String) redisTemplate.opsForHash().get(prefix + ":meta", "bucket");
        String key = (String) redisTemplate.opsForHash().get(prefix + ":meta", "key");

        CompletedMultipartUpload completed = CompletedMultipartUpload.builder()
                .parts(parts)
                .build();

        CompleteMultipartUploadRequest compReq = CompleteMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadId)
                .multipartUpload(completed)
                .build();

        CompleteMultipartUploadResponse response = s3Client.completeMultipartUpload(compReq);
        // 清理 redis
        redisTemplate.delete(Arrays.asList(prefix + ":meta", prefix + ":bitmap", prefix + ":etags"));
        log.info("completeUpload uploadId={} success={}", uploadId, response.sdkHttpResponse().isSuccessful());
        return response.sdkHttpResponse().isSuccessful();

    }


    //列出未上传分片
    public List<Integer> getMissingParts(String uploadId) {
        String prefix = keyPrefix(uploadId);
        String bitmapKey = prefix+":bitmap";
        List<Integer>missing = new ArrayList<>();
        String totalPartsStr = (String) redisTemplate.opsForHash().get(prefix + ":meta", "totalParts");
        int totalParts = 0;
        if (totalPartsStr != null) {
            totalParts = Integer.parseInt(totalPartsStr);
            for(int i = 1; i<=totalParts;i++){
                Boolean uploaded = redisTemplate.opsForValue().getBit(bitmapKey,i);
                if(!Boolean.TRUE.equals(uploaded)){
                    missing.add(i);
                }
            }
        }
        return missing;

    }


    // 中止上传
    public void abortUpload(String uploadId) {
        String prefix = keyPrefix(uploadId);
        String bucket = (String) redisTemplate.opsForHash().get(prefix + ":meta", "bucket");
        String key = (String) redisTemplate.opsForHash().get(prefix + ":meta", "key");
        String uploadIdFromMeta = (String) redisTemplate.opsForHash().get(prefix + ":meta", "uploadId");

        if (bucket == null || key == null || uploadIdFromMeta == null) return;

        s3Client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(key)
                .uploadId(uploadIdFromMeta)
                .build());

        redisTemplate.delete(Arrays.asList(prefix + ":meta", prefix + ":bitmap", prefix + ":etags"));
    }


    public String getUrl(String bucket, String key,String endpoint,String accessKey,String secretKey) {
        try (S3Presigner presigner = S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .region(Region.US_EAST_1)
                .build()) {

            GetObjectPresignRequest presignRequest =
                    GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofHours(12))
                            .getObjectRequest(
                                    GetObjectRequest.builder()
                                            .bucket(bucket)
                                            .key(key)
                                            .build()
                            )
                            .build();

            String url = presigner.presignGetObject(presignRequest).url().toString();
            log.info("getUrl bucket={} key={} url={}", bucket, key, url);
            return url;
        }
    }

    public String getUploadIdByKey(String uploadIdKey) {
        Object v = redisTemplate.opsForValue().get(uploadIdKey);
        return v == null ? null : (String) v;
    }




}
