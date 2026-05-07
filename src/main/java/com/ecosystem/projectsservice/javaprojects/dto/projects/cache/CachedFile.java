package com.ecosystem.projectsservice.javaprojects.dto.projects.cache;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;


// используется в кеш сервисе, содержит в себе, в том числе, поля для управления запись на диск

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CachedFile {
    private String content;
    private String extension;
    private String name;
    private String constructedPath;
    private UUID id;

    // кешируем uuid владельца файла
    private UUID ownerUUID;

    private UUID projectId;




    // поля управления

    private boolean written;

    private long version;


}
