package com.ecosystem.projectsservice.javaprojects.service.processes.directories.directory_move;

import com.ecosystem.projectsservice.javaprojects.transport.external_events.data.ExternalEventData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectoryMoveExternalData implements ExternalEventData {

    private Long directoryId; // кого
    private String directoryName;


    private Long parent; // кому


}
