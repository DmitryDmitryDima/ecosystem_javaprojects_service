package com.ecosystem.projectsservice.javaprojects.utils.projects;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.ProjectDTO;
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








        // Готовим структуру в виде таблицы - генерируем сущности Structure member и внедряем зависимости
        Map<String, StructureMember> memberMap = prepareMembersTable(directories, files);

        // Извлекаем корень
        StructureMember root = memberMap.get("directory_"+project.getRoot().getId());

        projectDTO.setStructure(List.of(root));




        return projectDTO;



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

            StructureMember structureMember = new StructureMember();
            structureMember.setOriginalId(fileReadOnly.getId());
            structureMember.setId("file_"+fileReadOnly.getId());
            structureMember.setType("file");
            structureMember.setName(fileReadOnly.getName());
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
