package com.example.demo.Config;

import com.example.demo.Component.MinioProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class S3Config {
    @Autowired
    private MinioProperties minioProperties;


    @Bean(destroyMethod = "close")
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(minioProperties.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(minioProperties.getAccessKey(), minioProperties.getSecretKey())))
                .region(Region.US_EAST_1) // region 对 MinIO 无实际意义，随意设置
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true) // MinIO 需要 path 风格
                        .build())
                .build();
    }
}