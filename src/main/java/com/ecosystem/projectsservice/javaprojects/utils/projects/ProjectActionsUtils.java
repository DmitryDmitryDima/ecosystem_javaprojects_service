package com.ecosystem.projectsservice.javaprojects.utils.projects;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.ProjectDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.SimpleFileView;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.StructureMember;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectType;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ProjectActionsUtils {



    // метод вызывается из контекста @Transactional
    public ProjectDTO generateProjectDTOWithStructure(Project project,
                                                      List<DirectoryReadOnly> directories,
                                                      List<FileReadOnly> files){

        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setProjectType(project.getType());
        projectDTO.setStatus(project.getStatus());
        projectDTO.setName(project.getName());
        projectDTO.setAuthor(project.getUserUUID());




        // возвращаем последние 5 редактируемых (сохраненных) файлов
        List<SimpleFileView> recentFiles = files.stream()
                .sorted(Comparator.comparing(FileReadOnly::getUpdated_at))
                .limit(5)
                .map(file->SimpleFileView
                        .builder()
                        .id(file.getId())
                        .name(file.getName())
                        .path(file.getConstructed_path())
                        .build())
                .toList();

        projectDTO.setRecentFiles(recentFiles);

        System.out.println(recentFiles);








        // Готовим структуру в виде таблицы - генерируем сущности Structure member и внедряем зависимости
        Map<String, StructureMember> memberMap = prepareMembersTable(directories, files);







        // тут мы даем возможность выбирать режим отображения в зависимости от типа проекта
        projectDTO.setStructure(getProjectSpecificLayerOfVisibility(memberMap, project.getRoot().getId(), project.getType()));




        return projectDTO;



    }



    private List<StructureMember> getProjectSpecificLayerOfVisibility(Map<String, StructureMember> table, Long rootId, ProjectType type){
        StructureMember root = table.get("directory_"+rootId);
        if (type==ProjectType.MAVEN_CLASSIC){
            StructureMember current = root;
            List<String> mavenHiddenLayers = List.of("src", "main");


            for (String hiddenlayer:mavenHiddenLayers){
                current = current.getChildren().stream().filter(structureMember
                        -> structureMember.getType().equals("directory")&&structureMember.getName().equals(hiddenlayer))
                        .findFirst().orElseThrow(()->new IllegalStateException("Структура maven некорректна"));

            }


            return current.getChildren().stream().
                    filter(structureMember ->
                                    (structureMember.getName().equals("java")|| structureMember.getName().equals("resources"))
                                            && structureMember.getType().equals("directory")).toList();
        }


        else return List.of(root);
    }

    // готовим таблицу
    private Map<String, StructureMember> prepareMembersTable(List<DirectoryReadOnly> directories,List<FileReadOnly> files ){

        Map<String, StructureMember> table = new HashMap<>();

        for (DirectoryReadOnly directoryReadOnly:directories){
            StructureMember structureMember = new StructureMember();
            structureMember.setOriginalId(directoryReadOnly.getId());
            structureMember.setId("directory_"+directoryReadOnly.getId());
            structureMember.setType("directory");
            structureMember.setName(directoryReadOnly.getName());
            structureMember.setImmutable(directoryReadOnly.isImmutable());

            table.put(structureMember.getId(), structureMember);


        }

        // для файлов можем начать вставлять зависимости, так как директории готовы
        for (FileReadOnly fileReadOnly:files){

            if (fileReadOnly.isHidden()) continue;

            StructureMember structureMember = new StructureMember();
            structureMember.setOriginalId(fileReadOnly.getId());
            structureMember.setId("file_"+fileReadOnly.getId());
            structureMember.setType("file");
            structureMember.setName(fileReadOnly.getName()+"."+(fileReadOnly.getExtension()==null?"":fileReadOnly.getExtension()));
            structureMember.setImmutable(fileReadOnly.isImmutable());

            table.put(structureMember.getId(), structureMember);

            // вставляем зависимость
            StructureMember parent = table.get("directory_"+fileReadOnly.getParent_id());

            parent.getChildren().add(structureMember);


        }

        // создаем зависимости между директориями
        for (DirectoryReadOnly directoryReadOnly:directories){

            if (directoryReadOnly.getParent_id()==null){
                continue;
            }

            StructureMember parent = table.get("directory_"+directoryReadOnly.getParent_id());
            StructureMember child = table.get("directory_"+directoryReadOnly.getId());

            parent.getChildren().add(child);


        }



        return table;

    }







}
