package com.ecosystem.projectsservice.javaprojects;


import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.ProjectDTO;
import com.ecosystem.projectsservice.javaprojects.model.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryJDBCRepository;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryRepository;
import com.ecosystem.projectsservice.javaprojects.service.ProjectActionsService;
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


    @Test
    public void testReading(){
        long id = 83;
        try {
            ProjectDTO dto = service.readProject(null,null,id);
            System.out.println(dto.getStructure());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void recursiveApproach(){
        Long id = 344L;
        List<DirectoryReadOnly> directories = directoryJDBCRepository.loadAWholeStructureFromRoot(id);

        System.out.println(directories);

        List<FileReadOnly> files = directoryJDBCRepository.loadFilesAssosiatedWithDirectories(directories
                .stream().map(DirectoryReadOnly::getId).toList());

        System.out.println(files);





    }
}
