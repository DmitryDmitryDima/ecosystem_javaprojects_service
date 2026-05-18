package com.ecosystem.projectsservice.javaprojects.service.projects.state.read;


import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.StructureSnapshot;
import com.ecosystem.projectsservice.javaprojects.model.read_only.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.read_only.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.repository.DirectoryJDBCRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// гибкий сервис для работы со снимками структуры
// todo можно добавить глубину уровня




@Service
@Transactional // внимание - все public методы сервиса - Transactional
public class SnapshotService {

    @Autowired
    private DirectoryJDBCRepository directoryJDBCRepository;

    // снимок представляет собой все, что является ребенком root (root входит в ответ)
    public StructureSnapshot getFullChildrenSnapshot(UUID root){
        // извлекаем все папки, принадлежащие проекту, вместе с зависимостями
        List<DirectoryReadOnly> directories = directoryJDBCRepository.loadAWholeStructureBelowRoot(root);
        System.out.println(directories);
        // извлекаем все файлы, принадлежащие проекту
        List<FileReadOnly> files = directoryJDBCRepository.loadFilesAssosiatedWithDirectories(
                directories.stream().map(DirectoryReadOnly::getId).toList()
        );
        System.out.println(files);
        return StructureSnapshot.builder()
                .directories(directories)
                .files(files)
                .build();

    }

    public StructureSnapshot getFullParentsSnapshot(UUID root){
        List<DirectoryReadOnly> directories = directoryJDBCRepository.loadAWholeStructureAboveRoot(root);
        List<FileReadOnly> files = directoryJDBCRepository.loadFilesAssosiatedWithDirectories(
                directories.stream().map(DirectoryReadOnly::getId).toList()
        );
        return StructureSnapshot.builder().directories(directories).files(files).build();
    }



    public List<DirectoryReadOnly> getParentsSnapshotDirectoriesOnly(UUID root){
        return directoryJDBCRepository.loadAWholeStructureAboveRoot(root);
    }

    public List<DirectoryReadOnly> getChildrenSnapshotDirectoriesOnly(UUID root){
        return directoryJDBCRepository.loadAWholeStructureBelowRoot(root);
    }

    public List<FileReadOnly> getFilesForDirectory(UUID root){
        return directoryJDBCRepository.loadFilesAssosiatedWithDirectories(List.of(root));
    }

    // все файлы вниз по ветке
    public List<FileReadOnly> getAllFilesBelowDirectory(UUID root){
        return directoryJDBCRepository.loadFilesBelowRoot(root);
    }

    public Optional<FileReadOnly> getFileBelowDirectory(UUID root, UUID fileId){
        return directoryJDBCRepository.loadFileBelowRoot(root, fileId);
    }

    public List<DirectoryReadOnly> getChildrenSnapshotDirectoriesOnlyWithLevel(UUID root, Long level){
        return directoryJDBCRepository.loadAWholeStructureBelowRootWithLevel(root, level);
    }

    public List<DirectoryReadOnly> getParentsSnapshotDirectoriesOnlyWithLevel(UUID root, Long level){
        return directoryJDBCRepository.loadAWholeStructureAboveRootWithLevel(root, level);
    }

    // метаданные всех файлов в проекте
    public List<FileReadOnly> getProjectFiles(UUID project){
        return directoryJDBCRepository.loadAllProjectFiles(project);
    }

    // поиск конкретного файла в проекте
    public Optional<FileReadOnly> getProjectFile(UUID project, UUID file){

        System.out.println("called "+project+" and "+file);

        return directoryJDBCRepository.loadProjectFile(project, file);
    }






}
