package com.ecosystem.projectsservice.javaprojects.service.storage;

import java.util.List;

public interface UserContentStorage {


    void save(String key, String content);

    void delete(String key);

    void deleteBatch(List<String> keys);


    String downloadContent(String key);

}
