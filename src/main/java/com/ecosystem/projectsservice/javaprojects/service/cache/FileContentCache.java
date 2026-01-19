package com.ecosystem.projectsservice.javaprojects.service.cache;

import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FileContentCache<ContentData, Id> {

    void save(@NotNull Long id, ContentData contentData);

    Optional<ContentData> read(Id id);

    List<ContentData> readAll();

    List<ContentData> readAllByLastActivity(Long seconds);

    List<CacheValueWrapper<ContentData>> readAllEntries();

    void remove(Id id);

    void removeAll(List<Id> ids);







}
