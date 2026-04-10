package com.ecosystem.projectsservice.javaprojects.configuration;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfiguration {


    /*

     */
    @Bean
    public RedisTemplate<String, FileDTO> fileDTOCacheTemplate(RedisConnectionFactory connectionFactory){

        RedisTemplate<String, FileDTO> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(RedisSerializer.string());

        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(FileDTO.class));




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


    @Bean
    @Qualifier("updateFieldIfKeyExists")
    public RedisScript<Boolean> updateFieldIfExistsScript(){

        String scriptBody = """
                if redis.call("EXISTS", KEYS[1]) == 1 then 
                redis.call("HSET", KEYS[1], ARGV[1], ARGV[2]) 
                return 1
                else 
                return 0
                end 
                """;


        return RedisScript.of(scriptBody, Boolean.class);
    }






}
