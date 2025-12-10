package com.ecosystem.projectsservice.javaprojects.utils.projects;

import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.ProjectDTO;
import com.ecosystem.projectsservice.javaprojects.dto.projects.actions.StructureMember;
import com.ecosystem.projectsservice.javaprojects.model.Directory;
import com.ecosystem.projectsservice.javaprojects.model.Project;
import com.ecosystem.projectsservice.javaprojects.model.enums.ProjectType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Component
public class ProjectActionsUtils {



    public ProjectDTO generateProjectDTOWithStructure(Project project){

        ProjectDTO projectDTO = new ProjectDTO();
        projectDTO.setProjectType(project.getType());
        projectDTO.setStatus(project.getStatus());
        projectDTO.setName(project.getName());

        if (project.getType()== ProjectType.MAVEN_CLASSIC){
            List<StructureMember> structure = generateStructureForMavenClassic(project);
            projectDTO.setStructure(structure);
        }

        return projectDTO;



    }

    // структура maven проекта начинается с папок java и resources. Для пользователя именно она будет корневой,
    // так как над всем остальным будет организован ограниченный контроль (пример - pom xml будет доступен напрямую через кнопку)
    private List<StructureMember> generateStructureForMavenClassic(Project project){


        List<String> hiddenFolders = List.of("src", "main");

        Directory current =  project.getRoot();


        // получаем папку main
        for (String hiddenFolder:hiddenFolders){
            current = current.getChildren().stream()
                    .filter((member)->member.getName().equals(hiddenFolder))
                    .findAny()
                    .orElseThrow(()->new IllegalStateException("invalid project structure"));
        }

        List<StructureMember> startingNodes = new ArrayList<>();

        for (Directory directory:current.getChildren()){
            if (directory.getName().equals("java") || directory.getName().equals("resources")){
                startingNodes.add(transformDirectory(directory));
            }
        }






        return  startingNodes;
    }


    private StructureMember transformDirectory(Directory directory){

        List<Directory> stack = new ArrayList<>();
        HashSet<Long> visited = new HashSet<>();
        stack.add(directory);
        while (!stack.isEmpty()){
            Directory member = stack.removeLast();
            System.out.println(member.getName());
            if (!visited.contains(member.getId())){
                visited.add(directory.getId());
                stack.addAll(member.getChildren());
            }



        }
        return null;
    }


}
