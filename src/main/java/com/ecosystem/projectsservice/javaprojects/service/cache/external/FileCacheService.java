package com.ecosystem.projectsservice.javaprojects.service.cache.external;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class FileCacheService implements FileCache{

    @Autowired
    private RedisTemplate<String, FileDTO> fileCacheTemplate;


    @Autowired
    private RedisTemplate<String, Object> redisTemplate;





    private final long expirationTimeInSec = 60;


    @Override
    public boolean saveIfPresent(FileDTO fileDTO) {






        if (fileDTO.getId() == null){
            throw new IllegalStateException("missing file id");
        }


        return Boolean.TRUE.equals(fileCacheTemplate
                        .opsForValue()
                        .setIfPresent(createKey(fileDTO.getId()),
                                fileDTO, expirationTimeInSec, TimeUnit.SECONDS));
    }

    @Override
    public void saveOrUpdate(FileDTO fileDTO) {

        if (fileDTO.getId() == null){
            throw new IllegalStateException("missing file id");
        }





        fileCacheTemplate
                .opsForValue()
                .set(createKey(fileDTO.getId()), fileDTO, expirationTimeInSec, TimeUnit.SECONDS);
    }

    @Override
    public Optional<FileDTO> get(Long id) {


        FileDTO dto = fileCacheTemplate.opsForValue().get(createKey(id));


        return dto==null?Optional.empty():Optional.of(dto);
    }

    @Override
    public boolean delete(Long id) {

        return fileCacheTemplate.delete(createKey(id));
    }

    @Override
    public boolean updateContent(Long key, String content) {





        return false;
    }






    private String createKey(Long id){
        return "file:"+id;
    }
}
