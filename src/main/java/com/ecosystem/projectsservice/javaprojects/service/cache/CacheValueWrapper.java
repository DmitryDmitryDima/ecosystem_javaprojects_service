package com.ecosystem.projectsservice.javaprojects.service.cache;

import lombok.Data;

import java.time.Instant;

@Data
public class CacheValueWrapper<Value>{
    private boolean locked;
    private Instant lastUpdate;
    private Value value;

    public CacheValueWrapper(Value value){
        this.value = value;
        lastUpdate = Instant.now();
    }


}
