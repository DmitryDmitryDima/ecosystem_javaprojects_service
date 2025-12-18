package com.ecosystem.projectsservice.javaprojects.utils.projects;


import com.ecosystem.projectsservice.javaprojects.model.Project;

import java.nio.file.Files;
import java.nio.file.Path;

// тут будем собирать самые общие и самые мелкие методы
public class ProjectUtils {


    public static String readFile(Path path) throws Exception{

        return Files.readString(path);
    }

    public static Path constructPathToFile(String userStorage, Project project, String relativeFilePath){
        return Path.of(userStorage, project.getUserUUID().toString(), "projects", relativeFilePath);
    }
}
