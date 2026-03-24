package com.ecosystem.projectsservice.javaprojects.service.processes.files.file_move;


import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMoveExternalData implements ExternalEventData {

    private Long fileId; // кого
    private String filename;
    private String directoryName;
    private String extension;
    private Long parent; // кому

    private String constructedPath;
}
