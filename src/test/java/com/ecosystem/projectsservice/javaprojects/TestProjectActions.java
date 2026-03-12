package com.ecosystem.projectsservice.javaprojects;


import com.ecosystem.projectsservice.javaprojects.model.read_only.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.read_only.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryJDBCRepository;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.service.projects.ProjectActionsService;
import com.ecosystem.projectsservice.javaprojects.service.projects.SnapshotService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class TestProjectActions {

    @Autowired
    private ProjectActionsService service;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private DirectoryJDBCRepository directoryJDBCRepository;


    @Autowired
    private SnapshotService snapshotService;





    @Test
    public void recursiveApproach(){
        Long id = 344L;
        List<DirectoryReadOnly> directories = directoryJDBCRepository.loadAWholeStructureFromRoot(id);

        System.out.println(directories);

        List<FileReadOnly> files = directoryJDBCRepository.loadFilesAssosiatedWithDirectories(directories
                .stream().map(DirectoryReadOnly::getId).toList());

        System.out.println(files);





    }

    @Test
    public void snapshotService(){
        System.out.println(snapshotService.getSnapshot(475L));
    }

}
