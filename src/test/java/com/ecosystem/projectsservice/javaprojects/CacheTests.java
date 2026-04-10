package com.ecosystem.projectsservice.javaprojects;


import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import com.ecosystem.projectsservice.javaprojects.model.read_only.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.service.projects.SnapshotService;
import com.ecosystem.projectsservice.javaprojects.service.projects.access_validation.ProjectAccessValidator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class CacheTests {

    @Autowired
    private ProjectAccessValidator validator;

    @Autowired
    private SnapshotService snapshotService;

    @Autowired
    private RedisTemplate<String, FileDTO> fileCache;

    @Autowired
    private RedisTemplate<String, Object> commonRedisTemplate;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    @Qualifier("updateFieldIfKeyExists")
    private RedisScript<Boolean> updateIfExistsScript;

    @Test
    public void cacheTest(){

        /*
        Long id = 8L;
        FileDTO fileDTO = FileDTO
                .builder()
                .id(id)
                .name("test")
                .extension("java")
                .ownerUUID(UUID.randomUUID())
                .constructedPath("/g/g")
                .content("blah")
                .projectId(UUID.randomUUID())
                .build();

        boolean success = Boolean.TRUE.equals(fileCache
                .opsForValue().setIfPresent("file:"+id, fileDTO, 60, TimeUnit.SECONDS));

        System.out.println(success);

        if (!success){
            // db validation call
            fileCache.opsForValue().set("file:"+id, fileDTO, 60, TimeUnit.SECONDS);
        }

        FileDTO retrieved = fileCache.opsForValue().get("file:"+8L);

        System.out.println(retrieved);


         */

        FileDTO fileDTO = FileDTO
                .builder()
                .id(8L)
                .name("test")
                .extension("java")
                .ownerUUID(UUID.randomUUID())
                .constructedPath("/g/g")
                .content("blah")
                .projectId(UUID.randomUUID())
                .build();


        Boolean answer = commonRedisTemplate.execute(updateIfExistsScript,
                List.of("file:8"), "content", "hello again");

        System.out.println("before save: "+answer);

        commonRedisTemplate.opsForHash().putAll("file:8", mapper.convertValue(fileDTO,
                new TypeReference<Map<String, String>>() {}));

        commonRedisTemplate.expire("file:8", 30, TimeUnit.SECONDS);

        answer = commonRedisTemplate.execute(updateIfExistsScript,
                List.of("file:8"), "content", "hello again");

        System.out.println("after update "+answer);

        Object obj = commonRedisTemplate.opsForHash().get("file:8", "content");

        String content = (String) obj;//mapper.convertValue(obj, String.class);
        System.out.println(content);

        FileDTO dto = mapper.convertValue(commonRedisTemplate.opsForHash().entries("file:8"),  FileDTO.class);

        System.out.println(dto);















    }
}
