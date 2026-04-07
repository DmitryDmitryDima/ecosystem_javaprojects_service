package com.ecosystem.projectsservice.javaprojects.utils.projects;


import com.ecosystem.projectsservice.javaprojects.model.Project;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

// тут будем собирать самые общие и самые мелкие методы
public class ProjectUtils {


    public static String readFile(Path path) throws Exception{

        return Files.readString(path);
    }

    public static Path constructPathToFile(String userStorage, UUID author, String relativeFilePath){
        return Path.of(userStorage, author.toString(), "projects", relativeFilePath);
    }
}
