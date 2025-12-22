package com.ecosystem.projectsservice.javaprojects.processes.chains.file_save;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSaveInfo {
    private Long fileId;
    private String projectsPath;
    private String content;
    private Long projectId;

    // todo participants


}
