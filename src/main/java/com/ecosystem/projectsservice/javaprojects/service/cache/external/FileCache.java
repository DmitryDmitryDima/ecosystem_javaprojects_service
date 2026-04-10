package com.ecosystem.projectsservice.javaprojects.service.cache.external;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.FileDTO;

import java.util.Optional;

public interface FileCache {

    boolean saveIfPresent(FileDTO fileDTO);

    void saveOrUpdate(FileDTO fileDTO);

    Optional<FileDTO> get(Long id);

    boolean delete(Long id);


    boolean updateContent(Long key, String content);



}
