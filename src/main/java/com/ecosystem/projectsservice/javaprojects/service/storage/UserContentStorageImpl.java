package com.ecosystem.projectsservice.javaprojects.service.storage;


import com.ecosystem.projectsservice.javaprojects.service.external_values.StorageExternals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserContentStorageImpl
        implements UserContentStorage
{

    @Autowired
    private StorageExternals storageExternals;

    @Autowired
    private StorageService service;


    @Override
    public void save(String key, String content) {
        service
                .saveOrUpdate(storageExternals.getStorageUserBucket(), key, content);
    }

    @Override
    public void delete(String key) {
        service.delete(storageExternals.getStorageUserBucket(), key);
    }

    @Override
    public void deleteBatch(List<String> keys) {
        service.deleteBatch(storageExternals.getStorageUserBucket(), keys);
    }

    @Override
    public String downloadContent(String key) {
        return service.downloadContent(storageExternals.getStorageUserBucket(), key);
    }
}
