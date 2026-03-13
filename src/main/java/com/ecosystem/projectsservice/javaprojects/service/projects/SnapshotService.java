package com.ecosystem.projectsservice.javaprojects.service.projects;


import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.StructureSnapshot;
import com.ecosystem.projectsservice.javaprojects.model.read_only.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.read_only.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryJDBCRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

// гибкий сервис для работы со снимками структуры
// todo можно добавить глубину уровня
@Service
public class SnapshotService {

    @Autowired
    private DirectoryJDBCRepository directoryJDBCRepository;

    // снимок представляет собой все, что является ребенком root (root входит в ответ)
    public StructureSnapshot getFullChildrenSnapshot(Long root){
        // извлекаем все папки, принадлежащие проекту, вместе с зависимостями
        List<DirectoryReadOnly> directories = directoryJDBCRepository.loadAWholeStructureBelowRoot(root);
        System.out.println(directories);
        // извлекаем все файлы, принадлежащие проекту
        List<FileReadOnly> files = directoryJDBCRepository.loadFilesAssosiatedWithDirectories(
                directories.stream().map(DirectoryReadOnly::getId).toList()
        );
        return StructureSnapshot.builder()
                .directories(directories)
                .files(files)
                .build();

    }

    public StructureSnapshot getFullParentsSnapshot(Long root){
        List<DirectoryReadOnly> directories = directoryJDBCRepository.loadAWholeStructureAboveRoot(root);
        List<FileReadOnly> files = directoryJDBCRepository.loadFilesAssosiatedWithDirectories(
                directories.stream().map(DirectoryReadOnly::getId).toList()
        );
        return StructureSnapshot.builder().directories(directories).files(files).build();
    }

    public List<DirectoryReadOnly> getParentsSnapshotDirectoriesOnly(Long root){
        return directoryJDBCRepository.loadAWholeStructureAboveRoot(root);
    }

    public List<DirectoryReadOnly> getChildrenSnapshotDirectoriesOnly(Long root){
        return directoryJDBCRepository.loadAWholeStructureBelowRoot(root);
    }

    public List<FileReadOnly> getFilesForDirectory(Long root){
        return directoryJDBCRepository.loadFilesAssosiatedWithDirectories(List.of(root));
    }






}
