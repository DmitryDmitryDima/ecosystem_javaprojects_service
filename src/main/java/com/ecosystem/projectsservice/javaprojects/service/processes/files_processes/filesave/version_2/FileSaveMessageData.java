package com.ecosystem.projectsservice.javaprojects.service.processes.files_processes.filesave.version_2;

import com.ecosystem.projectsservice.javaprojects.external_messaging.data.ExternalData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileSaveMessageData extends ExternalData {

    private UUID fileId;
    private String name;
    private String extension;
    private String content;

    private UUID fileOwner;
}
