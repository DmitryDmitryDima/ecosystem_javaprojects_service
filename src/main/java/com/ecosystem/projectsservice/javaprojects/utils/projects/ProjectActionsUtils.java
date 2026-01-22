package com.ecosystem.projectsservice.javaprojects.utils.projects;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.ProjectDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.ProjectSnapshot;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.SimpleFileInfo;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.StructureMember;
import com.ecosystem.projectsservice.javaprojects.model.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectType;
import org.springframework.stereotype.Component;

import java.util.*;

/*
анализ и интерпретация данных, вытащенных из бд
 */
@Component
public class ProjectActionsUtils {



    // метод вызывается из контекста @Transactional
    public void generateStructureForDTO(Long rootId, ProjectDTO projectDTO,
                                              ProjectSnapshot snapshot){








        // Готовим структуру в виде таблицы - генерируем сущности Structure member и внедряем зависимости
        Map<String, StructureMember> memberMap = prepareMembersTable(snapshot);







        // тут мы даем возможность выбирать режим отображения в зависимости от типа проекта
        projectDTO.setStructure(getProjectSpecificLayerOfVisibility(memberMap, rootId, projectDTO.getProjectType()));








    }

    public List<SimpleFileInfo> getRecentFiles(ProjectSnapshot snapshot){
        return snapshot.getFiles().stream()
                .sorted(Comparator.comparing(FileReadOnly::getUpdated_at).reversed())
                .filter(file->!file.isHidden())
                .limit(5)
                .map(file-> SimpleFileInfo
                        .builder()
                        .id(file.getId())
                        .name(file.getName())
                        .extension(file.getExtension())
                        .path(file.getConstructed_path())
                        .build())
                .toList();
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
    private Map<String, StructureMember> prepareMembersTable(ProjectSnapshot snapshot ){

        Map<String, StructureMember> table = new HashMap<>();

        for (DirectoryReadOnly directoryReadOnly: snapshot.getDirectories()){
            StructureMember structureMember = new StructureMember();
            structureMember.setOriginalId(directoryReadOnly.getId());
            structureMember.setId("directory_"+directoryReadOnly.getId());
            structureMember.setType("directory");
            structureMember.setName(directoryReadOnly.getName());
            structureMember.setImmutable(directoryReadOnly.isImmutable());

            table.put(structureMember.getId(), structureMember);


        }

        // для файлов можем начать вставлять зависимости, так как директории готовы
        for (FileReadOnly fileReadOnly: snapshot.getFiles()){

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
        for (DirectoryReadOnly directoryReadOnly: snapshot.getDirectories()){

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
