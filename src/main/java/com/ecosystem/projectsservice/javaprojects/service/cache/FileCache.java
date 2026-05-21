package com.ecosystem.projectsservice.javaprojects.service.cache;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.cache.CachedFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileCache {



    void saveOrUpdate(CachedFile cachedFile);

    Optional<CachedFile> get(UUID id);

    boolean delete(UUID id);


    boolean updateContent(UUID id, String content);


    List<CachedFile> scan();

    Long deleteCollection(List<UUID> keys);



}
