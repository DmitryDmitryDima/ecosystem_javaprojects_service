package com.ecosystem.projectsservice.javaprojects.service.cache;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FileContentCacheImpl implements FileContentCache<FileDTO, Long>{




    private final Map<Long, CacheValueWrapper<FileDTO>> cache = new ConcurrentHashMap<>();

    @Override
    public void save(Long id, FileDTO dto) {


        // атомарные проверки внутри hashmap
        cache.compute(id, (k,v)->{

            // если запись есть, проверяем, не заблокирована ли она
            if (v!=null) {
                if (v.isLocked()){
                    throw new LockedValueException("locked");
                }
                v.setLastUpdate(Instant.now());
                v.setValue(dto);
                return v;


            }

            else {
                return new CacheValueWrapper<>(dto);
            }






        });
    }

    @Override
    public Optional<FileDTO> read(Long id) {
        CacheValueWrapper<FileDTO> wrapper = cache.get(id);
        if (wrapper == null) return Optional.empty();
        else {
            return Optional.of(wrapper.getValue());
        }
    }

    @Override
    public List<FileDTO> readAll() {
        return cache.values().stream().map(CacheValueWrapper::getValue).toList();
    }

    @Override
    public void remove(Long id) {
        cache.remove(id);
    }

    @Override
    public void removeAll(List<Long> ids) {

    }

    @Override
    public boolean lock(Long aLong) {
        return false;
    }


}
