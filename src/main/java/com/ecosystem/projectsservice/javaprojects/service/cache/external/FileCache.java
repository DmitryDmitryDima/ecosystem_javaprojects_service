package com.ecosystem.projectsservice.javaprojects.service.cache.external;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileCache {



    void saveOrUpdate(FileDTO fileDTO);

    Optional<FileDTO> get(UUID id);

    boolean delete(UUID id);


    boolean updateContent(UUID id, String content);


    List<FileDTO> scan();



}
