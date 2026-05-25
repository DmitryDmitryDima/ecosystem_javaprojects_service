package com.ecosystem.projectsservice.javaprojects.service.cache;


import com.ecosystem.projectsservice.javaprojects.dto.projects.cache.CachedFile;
import com.ecosystem.projectsservice.javaprojects.dto.projects.cache.CachedJavaStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class JavaStructureCacheService implements JavaStructureCache {


    private final long expirationTimeInSec = 60*10;

    private final String keyPattern = "project:structure:";


    @Autowired
    private ObjectMapper mapper;


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;



    @Override
    public void save(CachedJavaStructure structure) {
        if (structure.getId() == null) {
            throw new IllegalStateException("missing id");

        }

        redisTemplate.opsForHash().putAll(createKey(structure.getId()),
                mapper.convertValue(structure,


                        new TypeReference<Map<String, String>>() {}));




        // период устаревания кеша
        redisTemplate.expire(createKey(structure.getId()),
                expirationTimeInSec, TimeUnit.SECONDS);

    }

    @Override
    public Optional<CachedJavaStructure> get(UUID id) {
        Map<Object, Object> hash = redisTemplate.opsForHash().entries(createKey(id));



        if (hash.isEmpty()){
            return Optional.empty();
        }
        else {
            return Optional.of(mapper.convertValue(hash, CachedJavaStructure.class));
        }
    }

    @Override
    public boolean delete(UUID id) {
        return redisTemplate.delete(createKey(id));
    }

    private String createKey(UUID id){
        return keyPattern+id;
    }
}
