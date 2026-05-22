package com.ecosystem.projectsservice.javaprojects.service.cache;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;


import com.ecosystem.projectsservice.javaprojects.dto.projects.cache.CachedFile;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;

import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;


import java.time.Instant;
import java.util.*;
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


    @Autowired
    @Qualifier("updateFileWrittenField")
    private RedisScript<Boolean> updateFileWrittenField;








    private final long expirationTimeInSec = 60*10;

    private final String keyPattern = "file:";




    @Override
    public void saveOrUpdate(CachedFile cachedFile) {



        if (cachedFile.getId() == null){
            throw new IllegalStateException("missing file id");
        }

        redisTemplate.opsForHash().putAll(createKey(cachedFile.getId()),
                mapper.convertValue(cachedFile,


                new TypeReference<Map<String, String>>() {}));




        // период устаревания кеша
        redisTemplate.expire(createKey(cachedFile.getId()),
                expirationTimeInSec, TimeUnit.SECONDS);






    }

    @Override
    public Optional<CachedFile> get(UUID id) {



        Map<Object, Object> hash = redisTemplate.opsForHash().entries(createKey(id));



        if (hash.isEmpty()){
            return Optional.empty();
        }
        else {
            return Optional.of(mapper.convertValue(hash, CachedFile.class));
        }





    }

    @Override
    public boolean delete(UUID id) {

        return redisTemplate.delete(createKey(id));


    }




    /*
    при обновлении контента обновляется и expire time
     */
    @Override
    public boolean updateContent(UUID id, String content) {


        String key = createKey(id);
        String contentField = "content";










        return redisTemplate.execute(updateIfExistsScript,
                List.of(key), contentField, content, Long.toString(expirationTimeInSec),
                Instant.now().toString());
    }



    @Override
    public List<CachedFile> scan() {

        System.out.println("scan operation starts in cache");

        ScanOptions scanOptions = ScanOptions
                .scanOptions()
                .match(keyPattern+"*")
                .build();


        // шаг 1 - извлекаем все активные ключи в redis

        Set<String> keys = new HashSet<>();



        try (Cursor<String> cursor = redisTemplate.scan(scanOptions)){
            while (cursor.hasNext()){
                keys.add(cursor.next());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }


        List<Object> result = redisTemplate.executePipelined(new SessionCallback<>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {

                for (String key : keys) {
                    operations.opsForHash().entries(key);
                }
                return null;
            }
        });




        // шаг 2 - извлекаем по полученным ключам hash values
        return result.stream()
                .map(obj->mapper.convertValue(obj, CachedFile.class)).toList();


    }

    @Override
    public Long deleteCollection(List<UUID> keys) {

        return redisTemplate
                .unlink(keys.stream().map(this::createKey).toList());

    }


    // чтобы written проставить в true, мы должны убедиться внутри скрипта,
    // что version остался тем же
    @Override
    public boolean markAsWritten(UUID id, long version) {
        String key = createKey(id);

        return redisTemplate
                .execute(updateFileWrittenField, List.of(key), Long.toString(version));



    }


    private String createKey(UUID id){
        return keyPattern+id;
    }
}
