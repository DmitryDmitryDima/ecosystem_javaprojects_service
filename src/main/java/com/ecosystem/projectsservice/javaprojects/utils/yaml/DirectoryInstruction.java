package com.ecosystem.projectsservice.javaprojects.utils.yaml;

import com.ecosystem.projectsservice.javaprojects.model.Directory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DirectoryInstruction {
    private Long id;
    private String name;
    private boolean immutable;
    private boolean hidden;
    private Long parent;


    public Directory prepareDirectoryEntity(){
        Directory directory = new Directory();
        directory.setImmutable(immutable);
        directory.setName(name);
        directory.setCreatedAt(Instant.now());
        directory.setHidden(hidden);
        return directory;
    }
}
