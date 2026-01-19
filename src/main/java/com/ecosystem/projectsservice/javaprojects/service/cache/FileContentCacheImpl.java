package com.ecosystem.projectsservice.javaprojects.service.cache;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalField;
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
            wrapper.setLastUpdate(Instant.now());
            return Optional.of(wrapper.getValue());
        }
    }

    @Override
    public List<FileDTO> readAll() {
        return cache.values().stream()
                .map(CacheValueWrapper::getValue).toList();
    }

    @Override
    public List<FileDTO> readAllByLastActivity(Long seconds){
        return cache.values().stream().filter(entry-> Duration.between(entry.getLastUpdate(), Instant.now()).getSeconds()>=seconds)
                .map(CacheValueWrapper::getValue).toList();
    }

    @Override
    public List<CacheValueWrapper<FileDTO>> readAllEntries() {
        return cache.values().stream().toList();
    }

    @Override
    public void remove(Long id) {
        cache.remove(id);
    }

    @Override
    public void removeAll(List<Long> ids) {

    }




}
