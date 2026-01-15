package com.ecosystem.projectsservice.javaprojects.service.cache;

import java.util.Collection;
import java.util.List;

public interface FileContentCache<ContentData, Id> {

    void save(ContentData contentData);

    ContentData read(Id id);

    List<ContentData> readAll();

    void remove(Id id);

    void removeAll(List<Id> ids);

    public boolean lock(Id id);





}
