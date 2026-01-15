package com.ecosystem.projectsservice.javaprojects.service.cache;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FileContentCacheImpl implements FileContentCache<FileContent, Long>{

    private final Map<Long, FileContent> cache = new ConcurrentHashMap<>();

    @Override
    public void save(FileContent content) {
        if (content.getId()==null){
            throw new IllegalStateException("null id");
        }

        // атомарные проверки внутри hashmap
        cache.compute(content.getId(), (k,v)->{
            content.setLastUpdate(Instant.now());
            if (v!=null) {
                if (v.isLocked()){
                    throw new IllegalStateException("locked");
                }

            }
            return content;

        });
    }

    @Override
    public FileContent read(Long id) {
        return cache.get(id);
    }

    @Override
    public List<FileContent> readAll() {
        return cache.values().stream().toList();
    }

    @Override
    public void remove(Long id) {

    }

    @Override
    public void removeAll(List<Long> ids) {

    }

    @Override
    public boolean lock(Long aLong) {
        return false;
    }


}
