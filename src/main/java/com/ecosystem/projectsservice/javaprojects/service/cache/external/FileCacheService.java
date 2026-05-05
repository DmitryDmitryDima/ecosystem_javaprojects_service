package com.ecosystem.projectsservice.javaprojects.service.cache.external;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;


import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;

import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;


import java.lang.reflect.Type;
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







    private final long expirationTimeInSec = 60*10;

    private final String keyPattern = "file:";




    @Override
    public void saveOrUpdate(FileDTO fileDTO) {



        if (fileDTO.getId() == null){
            throw new IllegalStateException("missing file id");
        }

        redisTemplate.opsForHash().putAll(createKey(fileDTO.getId()), mapper.convertValue(fileDTO,


                new TypeReference<Map<String, String>>() {}));




        // период устаревания кеша
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




    /*
    при обновлении контента обновляется и expire time
     */
    @Override
    public boolean updateContent(Long id, String content) {


        String key = createKey(id);
        String contentField = "content";










        return redisTemplate.execute(updateIfExistsScript,
                List.of(key), contentField, content, Long.toString(expirationTimeInSec));
    }

    /*
    todo во избежание гонок необходимо обдумать внедрение комбинации поля written и version
    первое позволит избежать записи в диск уже записанных до этого данных,
    второе - позволит корректно проставить written, гарантируя,
    что во время записи в диск никто не обновил контент в кеше.
    Если это произошло - written не ставится и ожидается следующая итерация
     */

    @Override
    public List<FileDTO> scan() {

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
        return result.stream().map(obj->mapper.convertValue(obj, FileDTO.class)).toList();


    }


    private String createKey(Long id){
        return keyPattern+id;
    }
}
