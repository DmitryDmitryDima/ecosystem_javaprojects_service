package com.ecosystem.projectsservice.javaprojects.utils.projects;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.reading.*;
import com.ecosystem.projectsservice.javaprojects.model.enums.DirectoryStatus;
import com.ecosystem.projectsservice.javaprojects.model.read_only.DirectoryReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.read_only.FileReadOnly;
import com.ecosystem.projectsservice.javaprojects.model.enums.FileStatus;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectType;
import org.springframework.stereotype.Component;

import java.util.*;

/*
анализ и интерпретация данных, вытащенных из бд
 */
@Component
public class ProjectActionsUtils {




    public Optional<FileReadOnly> findAvailableFile(StructureSnapshot snapshot, Long toCheck){
        return snapshot.getFiles()
                .stream()
                .filter(fileReadOnly -> fileReadOnly.getId().equals(toCheck)
                        && !fileReadOnly.isHidden()
                        && fileReadOnly.getStatus().equals(FileStatus.AVAILABLE)).findFirst();
    }

    public Optional<DirectoryReadOnly> findAvailableDirectory(StructureSnapshot snapshot, Long toCheck){

        return snapshot.getDirectories()
                .stream()
                .filter(directoryReadOnly -> directoryReadOnly.getId().equals(toCheck)
                        && !directoryReadOnly.isHidden()
                        && directoryReadOnly.getStatus().equals(DirectoryStatus.AVAILABLE)).findFirst();
    }





    // метод вызывается из контекста @Transactional
    public void generateStructureForDTO(Long rootId, ProjectDTO projectDTO,
                                              StructureSnapshot snapshot){








        // Готовим структуру в виде таблицы - генерируем сущности Structure member и внедряем зависимости
        Map<String, StructureMember> memberMap = prepareMembersTable(snapshot, rootId);










        //projectDTO.setStructure(getProjectSpecificLayerOfVisibility(memberMap, rootId, projectDTO.getProjectType()));

        projectDTO.setStructure(memberMap.values().stream().filter(structureMember ->
                structureMember.getType().equals("directory") && structureMember.isHiddenParent() && !structureMember.isHidden()).toList());






    }

    public List<SimpleFileInfo> getRecentFiles(StructureSnapshot snapshot){
        return snapshot.getFiles().stream()
                .sorted(Comparator.comparing(FileReadOnly::getUpdated_at).reversed())
                .filter(file->!file.isHidden()&&file.getStatus()!=FileStatus.REMOVING)
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




        return table.values().stream().filter(structureMember ->
                structureMember.getType().equals("directory") && structureMember.isHiddenParent() && !structureMember.isHidden()).toList();

        /*
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

         */


    }

    // готовим таблицу
    private Map<String, StructureMember> prepareMembersTable(StructureSnapshot snapshot, Long rootId){

        Map<String, StructureMember> table = new HashMap<>();

        for (DirectoryReadOnly directoryReadOnly: snapshot.getDirectories()){


            StructureMember structureMember = new StructureMember();
            structureMember.setHidden(directoryReadOnly.isHidden());
            structureMember.setOriginalId(directoryReadOnly.getId());
            structureMember.setId("directory_"+directoryReadOnly.getId());
            structureMember.setType("directory");
            structureMember.setName(directoryReadOnly.getName());
            structureMember.setImmutable(directoryReadOnly.isImmutable());

            table.put(structureMember.getId(), structureMember);




        }

        // для файлов можем начать вставлять зависимости, так как директории готовы
        for (FileReadOnly fileReadOnly: snapshot.getFiles()){

            if (fileReadOnly.isHidden() || fileReadOnly.getStatus()== FileStatus.REMOVING) continue;

            StructureMember structureMember = new StructureMember();
            structureMember.setHidden(fileReadOnly.isHidden());
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

            System.out.println(parent.getName()+" parent is "+parent.isHidden()+" and child "+child.getName());
            child.setHiddenParent(parent.isHidden());

            parent.getChildren().add(child);




        }





        return table;

    }







}
