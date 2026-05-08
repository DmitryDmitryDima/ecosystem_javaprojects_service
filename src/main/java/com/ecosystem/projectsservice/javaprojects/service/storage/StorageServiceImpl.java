package com.ecosystem.projectsservice.javaprojects.service.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


@Service
public class StorageServiceImpl implements StorageService {



    @Autowired
    private S3Client storageClient;

    @Override
    public void saveOrUpdate(String bucket, String key, String content) {


        try {
            PutObjectRequest putObjectRequest =
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build();


            storageClient.putObject(putObjectRequest, RequestBody
                    .fromBytes(content.getBytes(StandardCharsets.UTF_8)));


        }

        catch (Exception e){
            throw new StorageException("Ошибка сохранения объекта: "+e.getMessage());
        }





    }

    @Override
    public void delete(String bucket, String key) {

        try {
            DeleteObjectRequest deleteObjectRequest
                    = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            storageClient.deleteObject(deleteObjectRequest);
        }

        catch (Exception e){
            throw new StorageException("Ошибка удаления объекта: "+e.getMessage());
        }



    }

    @Override
    public String downloadContent(String bucket, String key) {

        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> stream = storageClient.getObject(request)) {
            byte[] data = stream.readAllBytes();
            return new String(data, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new StorageException("ошибка скачивания объекта: "+e.getMessage());
        }
    }
}
