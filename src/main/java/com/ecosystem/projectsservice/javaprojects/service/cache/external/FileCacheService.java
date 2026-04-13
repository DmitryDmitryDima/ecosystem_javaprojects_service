package com.ecosystem.projectsservice.javaprojects.service.cache.external;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class FileCacheService implements FileCache{


    @Autowired
    private ObjectMapper mapper;


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    // LUA скрипт, производящий модификацию hash поля только при наличии ключа
    // в противном случае возвращает false - это требует db валидации действия
    @Autowired
    @Qualifier("updateFieldIfKeyExists")
    private RedisScript<Boolean> updateIfExistsScript;







    private final long expirationTimeInSec = 30;




    @Override
    public void saveOrUpdate(FileDTO fileDTO) {

        if (fileDTO.getId() == null){
            throw new IllegalStateException("missing file id");
        }

        redisTemplate.opsForHash().putAll(createKey(fileDTO.getId()), mapper.convertValue(fileDTO,
                new TypeReference<Map<String, String>>() {}));

        redisTemplate.expire(createKey(fileDTO.getId()), expirationTimeInSec, TimeUnit.SECONDS);






    }

    @Override
    public Optional<FileDTO> get(Long id) {



        Map<Object, Object> hash = redisTemplate.opsForHash().entries(createKey(id));

        if (hash.isEmpty()){
            return Optional.empty();
        }
        else {
            return Optional.of(mapper.convertValue(hash, FileDTO.class));
        }





    }

    @Override
    public boolean delete(Long id) {

        return redisTemplate.delete(createKey(id));
    }




    @Override
    public boolean updateContent(Long id, String content) {


        String key = createKey(id);
        String contentField = "content";










        return redisTemplate.execute(updateIfExistsScript,
                List.of(key), contentField, content);
    }






    private String createKey(Long id){
        return "file:"+id;
    }
}
