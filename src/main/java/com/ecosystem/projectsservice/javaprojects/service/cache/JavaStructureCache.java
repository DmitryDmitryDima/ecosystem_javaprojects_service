package com.ecosystem.projectsservice.javaprojects.service.cache;

import com.ecosystem.projectsservice.javaprojects.dto.projects.cache.CachedFile;
import com.ecosystem.projectsservice.javaprojects.dto.projects.cache.CachedJavaStructure;

import java.util.Optional;
import java.util.UUID;

public interface JavaStructureCache {

    void save(CachedJavaStructure structure);

    Optional<CachedJavaStructure> get(UUID id);


    boolean delete(UUID id);


}
