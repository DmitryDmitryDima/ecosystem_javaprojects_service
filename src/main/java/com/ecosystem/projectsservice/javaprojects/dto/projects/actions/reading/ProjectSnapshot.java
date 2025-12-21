package com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading;

import com.ecosystem.projectsservice.javaprojects.model.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.FileReadOnly;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProjectSnapshot {
    private List<DirectoryReadOnly> directories;
    private List<FileReadOnly> files;

}
