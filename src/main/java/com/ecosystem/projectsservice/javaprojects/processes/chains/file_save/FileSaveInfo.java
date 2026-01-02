package com.ecosystem.projectsservice.javaprojects.processes.chains.file_save;

import com.ecosystem.projectsservice.javaprojects.processes.chains.ChainEntranceInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSaveInfo implements ChainEntranceInfo {
    private Long fileId;
    private String projectsPath;
    private String content;
    private Long projectId;

    // todo participants


}
