package com.ecosystem.projectsservice.javaprojects.configuration;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.data.redis.core.script.RedisScript;

import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Instant;

@Configuration
public class RedisConfiguration {


    /*

     */
    @Bean
    public RedisTemplate<String, FileDTO> fileDTOCacheTemplate(RedisConnectionFactory connectionFactory){

        RedisTemplate<String, FileDTO> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(RedisSerializer.string());

        template.setValueSerializer(new JacksonJsonRedisSerializer<>(FileDTO.class));




        return template;
    }

    @Bean
    public RedisTemplate<String, Object> strKeyTemplate(RedisConnectionFactory connectionFactory){

        RedisTemplate<String, Object> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.string());
        template.setHashValueSerializer(RedisSerializer.string());









        return template;
    }



    // обноление поля файла в хеше. Если ключа нет - возвращается false.
    // каждое обновление инкрементирует поле version и вставляет актуальное значение last update
    // поле written сбрасывается в false, чтобы быть подхваченным scan
    //

    @Bean
    @Qualifier("updateFieldIfKeyExists")
    public RedisScript<Boolean> updateFieldIfExistsScript(){



        String scriptBody = """
                if redis.call("EXISTS", KEYS[1]) == 1 then 
                redis.call("HSET", KEYS[1], ARGV[1], ARGV[2])
                redis.call("HSET", KEYS[1], "lastUpdate", ARGV[4])
                redis.call("HSET", KEYS[1], "written", "false")
                redis.call("HINCRBY", KEYS[1], "version", 1) 
                redis.call("EXPIRE", KEYS[1], ARGV[3])
                return 1
                else 
                return 0
                end 
                """;


        return RedisScript.of(scriptBody, Boolean.class);
    }


    // если  версия совпадает с переданной - обновляем на true

    @Bean
    @Qualifier("updateFileWrittenField")
    public RedisScript<Boolean> updateFileWrittenField(){


        String script = """
                if redis.call("HGET", KEYS[1], "version") == ARGV[1]
                    then redis.call("HSET", KEYS[1], "written", "true")
                return 1
                else 
                return 0
                end
                
                
                """;


        return RedisScript.of(script, Boolean.class);
    }






}
